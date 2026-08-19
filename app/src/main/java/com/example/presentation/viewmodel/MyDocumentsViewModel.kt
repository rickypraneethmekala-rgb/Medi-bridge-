package com.example.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.alarm.MedicineAlarmScheduler
import com.example.core.storage.DocumentStorageHelper
import com.example.data.local.MediBridgeDatabase
import com.example.data.local.entity.MedicalDocument
import com.example.data.local.entity.MedicineReminder
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MyDocumentsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MediBridgeDatabase.getDatabase(application)
    private val repository = DocumentRepository(database.medicalDocumentDao(), database.medicineReminderDao())

    val documents: StateFlow<List<MedicalDocument>> = repository.allDocuments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reminders: StateFlow<List<MedicineReminder>> = repository.allReminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedDocument = MutableStateFlow<MedicalDocument?>(null)
    val selectedDocument: StateFlow<MedicalDocument?> = _selectedDocument.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun selectDocument(doc: MedicalDocument?) {
        _selectedDocument.value = doc
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun saveDocument(
        fileName: String,
        fileType: String,
        sourceUri: Uri,
        description: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            val context = getApplication<Application>()

            val ext = when (fileType.uppercase()) {
                "PDF" -> "pdf"
                "PNG" -> "png"
                "JPEG" -> "jpeg"
                else -> "jpg"
            }

            val savedResult = DocumentStorageHelper.saveFileToPrivateStorage(context, sourceUri, ext)
            if (savedResult != null) {
                val (docId, localPath) = savedResult
                val doc = MedicalDocument(
                    id = docId,
                    userId = "patient_user",
                    fileName = fileName.ifBlank { "Medical Document ${System.currentTimeMillis() % 10000}" },
                    fileType = fileType.uppercase(),
                    localFilePath = localPath,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.insertDocument(doc)
                _userMessage.value = "Document saved securely to local storage."
                onSuccess()
            } else {
                _userMessage.value = "Failed to copy file to local storage. Please try again."
            }
            _isProcessing.value = false
        }
    }

    fun updateDocument(doc: MedicalDocument, newFileName: String, newDescription: String) {
        viewModelScope.launch {
            val updated = doc.copy(
                fileName = newFileName.ifBlank { doc.fileName },
                description = newDescription,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateDocument(updated)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = updated
            }
            _userMessage.value = "Document updated."
        }
    }

    fun deleteDocument(doc: MedicalDocument) {
        viewModelScope.launch {
            // Cancel scheduled alarms for all reminders attached to this document
            val docReminders = reminders.value.filter { it.documentId == doc.id }
            val context = getApplication<Application>()
            for (rem in docReminders) {
                val (h, m) = MedicineAlarmScheduler.parseHourMinute(rem.time)
                MedicineAlarmScheduler.cancelAlarm(context, rem.id, h, m)
            }

            repository.deleteDocument(doc)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = null
            }
            _userMessage.value = "Document and local file deleted."
        }
    }

    fun addReminder(
        documentId: String,
        medicineName: String,
        timeString: String,
        instruction: String
    ) {
        viewModelScope.launch {
            val reminderId = UUID.randomUUID().toString()
            val reminder = MedicineReminder(
                id = reminderId,
                documentId = documentId,
                medicineName = medicineName.ifBlank { "Prescribed Medicine" },
                time = timeString.ifBlank { "08:00 AM" },
                instruction = instruction.ifBlank { "Take as directed" },
                isEnabled = true,
                createdAt = System.currentTimeMillis()
            )
            repository.insertReminder(reminder)

            val context = getApplication<Application>()
            val (h, m) = MedicineAlarmScheduler.parseHourMinute(reminder.time)
            MedicineAlarmScheduler.scheduleAlarm(
                context = context,
                reminderId = reminder.id,
                medicineName = reminder.medicineName,
                dosage = reminder.instruction,
                hour = h,
                minute = m
            )
            _userMessage.value = "Medicine reminder scheduled for ${reminder.time}."
        }
    }

    fun toggleReminder(reminder: MedicineReminder) {
        viewModelScope.launch {
            val newEnabled = !reminder.isEnabled
            val updated = reminder.copy(isEnabled = newEnabled)
            repository.updateReminder(updated)

            val context = getApplication<Application>()
            val (h, m) = MedicineAlarmScheduler.parseHourMinute(reminder.time)
            if (newEnabled) {
                MedicineAlarmScheduler.scheduleAlarm(
                    context = context,
                    reminderId = reminder.id,
                    medicineName = reminder.medicineName,
                    dosage = reminder.instruction,
                    hour = h,
                    minute = m
                )
                _userMessage.value = "Reminder enabled for ${reminder.time}."
            } else {
                MedicineAlarmScheduler.cancelAlarm(context, reminder.id, h, m)
                _userMessage.value = "Reminder paused."
            }
        }
    }

    fun deleteReminder(reminder: MedicineReminder) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val (h, m) = MedicineAlarmScheduler.parseHourMinute(reminder.time)
            MedicineAlarmScheduler.cancelAlarm(context, reminder.id, h, m)
            repository.deleteReminder(reminder)
            _userMessage.value = "Reminder removed."
        }
    }
}

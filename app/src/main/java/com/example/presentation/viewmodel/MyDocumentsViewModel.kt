package com.example.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.alarm.MedicineAlarmScheduler
import com.example.core.storage.DocumentStorageHelper
import com.example.data.local.MediBridgeDatabase
import com.example.data.local.entity.AccessLogEntity
import com.example.data.local.entity.EmergencyHealthInfoEntity
import com.example.data.local.entity.MedicalDocument
import com.example.data.local.entity.MedicineReminder
import com.example.data.local.entity.RecordShareEntity
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MyDocumentsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MediBridgeDatabase.getDatabase(application)
    private val repository = DocumentRepository(
        database.medicalDocumentDao(),
        database.medicineReminderDao(),
        database.recordShareDao(),
        database.accessLogDao(),
        database.emergencyHealthInfoDao()
    )

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

    val allShares: StateFlow<List<RecordShareEntity>> = repository.allShares
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeShares: StateFlow<List<RecordShareEntity>> = repository.activeShares
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val accessLogs: StateFlow<List<AccessLogEntity>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val emergencyInfo: StateFlow<EmergencyHealthInfoEntity?> = repository.emergencyInfo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EmergencyHealthInfoEntity()
        )

    private val _selectedDocument = MutableStateFlow<MedicalDocument?>(null)
    val selectedDocument: StateFlow<MedicalDocument?> = _selectedDocument.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        seedInitialDemoDataIfEmpty()
    }

    private fun seedInitialDemoDataIfEmpty() {
        viewModelScope.launch {
            // Seed emergency health info if missing
            val defaultEmergency = EmergencyHealthInfoEntity(
                id = "primary_emergency_info",
                bloodGroup = "O+ Positive",
                allergies = "Penicillin, Sulfa Antibiotics",
                chronicConditions = "Mild Essential Hypertension",
                currentMedicines = "Amlodipine 5mg OD, Vitamin D3 60K weekly",
                emergencyContactName = "Priya Sharma (Spouse)",
                emergencyContactPhone = "+91 98765 43210",
                isSharingAllowedInEmergency = true
            )
            repository.saveEmergencyInfo(defaultEmergency)

            // Seed initial records if empty
            val initialDocs = listOf(
                MedicalDocument(
                    id = "doc_cardio_rx_01",
                    userId = "local_patient",
                    fileName = "Cardiology Prescription",
                    fileType = "JPG",
                    localFilePath = "",
                    category = "Prescription",
                    hospitalOrDoctor = "Dr. Rajesh Sharma • Apollo City Hospital",
                    recordDate = "22 Sep 2026",
                    description = "Amlodipine 5mg (1 tab OD morning), Metoprolol 25mg (1 tab OD evening). Follow up in 2 weeks for blood pressure review.",
                    createdAt = System.currentTimeMillis() - 2 * 86400000L
                ),
                MedicalDocument(
                    id = "doc_lipid_lab_02",
                    userId = "local_patient",
                    fileName = "CBC & Lipid Profile Panel",
                    fileType = "PDF",
                    localFilePath = "",
                    category = "Lab Report",
                    hospitalOrDoctor = "Dr. Lal PathLabs",
                    recordDate = "20 Sep 2026",
                    description = "Total Cholesterol 185 mg/dL (Normal < 200), HDL 48 mg/dL, LDL 108 mg/dL. Fasting Blood Glucose 94 mg/dL.",
                    createdAt = System.currentTimeMillis() - 4 * 86400000L
                ),
                MedicalDocument(
                    id = "doc_discharge_03",
                    userId = "local_patient",
                    fileName = "Day-Care Discharge Summary",
                    fileType = "PDF",
                    localFilePath = "",
                    category = "Discharge Summary",
                    hospitalOrDoctor = "Care Multi-Speciality Hospital",
                    recordDate = "15 Sep 2026",
                    description = "Elective day-care endoscopy and health checkup. Vital parameters stable at discharge. Diet: Normal home cooked food.",
                    createdAt = System.currentTimeMillis() - 9 * 86400000L
                ),
                MedicalDocument(
                    id = "doc_bill_04",
                    userId = "local_patient",
                    fileName = "Hospital Outpatient Consultation Bill",
                    fileType = "JPG",
                    localFilePath = "",
                    category = "Medical Bill",
                    hospitalOrDoctor = "Apollo Hospitals Accounts",
                    recordDate = "22 Sep 2026",
                    description = "OPD Consultation Fee: ₹800. Paid via UPI. Star Health Insurance cashless pre-auth acknowledged.",
                    createdAt = System.currentTimeMillis() - 2 * 86400000L
                )
            )

            // Insert documents if not present
            for (doc in initialDocs) {
                if (repository.getDocumentById(doc.id) == null) {
                    repository.insertDocument(doc)
                }
            }

            // Seed active consent share if empty
            val initialShareId = "share_demo_sharma"
            val share = RecordShareEntity(
                id = initialShareId,
                documentId = "doc_cardio_rx_01",
                documentIdsJson = "[\"doc_cardio_rx_01\",\"doc_lipid_lab_02\"]",
                documentName = "Cardiology Prescription + CBC & Lipid Profile",
                documentCategory = "Prescription",
                recipientType = "Doctor",
                recipientName = "Dr. Rajesh Sharma (Cardiologist)",
                accessDuration = "7 Days",
                sharedAt = System.currentTimeMillis() - 12 * 3600 * 1000L,
                expiresAt = System.currentTimeMillis() + 6 * 86400000L,
                status = "ACTIVE",
                secureToken = "MB-7492-EXP",
                purpose = "Follow-up OPD Consultation"
            )
            repository.insertShare(share)

            // Seed demo access audit logs
            repository.insertLog(
                AccessLogEntity(
                    id = "log_01",
                    shareId = initialShareId,
                    documentTitle = "Cardiology Prescription",
                    accessorName = "Dr. Rajesh Sharma",
                    accessorRole = "Doctor",
                    action = "ACCESSED",
                    timestamp = System.currentTimeMillis() - 2 * 3600 * 1000L,
                    status = "Authorized",
                    verificationMethod = "Time-Limited QR Code"
                )
            )
            repository.insertLog(
                AccessLogEntity(
                    id = "log_02",
                    shareId = initialShareId,
                    documentTitle = "CBC & Lipid Profile Panel",
                    accessorName = "Apollo City Hospital OPD",
                    accessorRole = "Hospital",
                    action = "DOWNLOADED",
                    timestamp = System.currentTimeMillis() - 5 * 3600 * 1000L,
                    status = "Authorized",
                    verificationMethod = "Consent Token"
                )
            )
            repository.insertLog(
                AccessLogEntity(
                    id = "log_03",
                    shareId = "share_demo_prev",
                    documentTitle = "Day-Care Discharge Summary",
                    accessorName = "Care Multi-Speciality",
                    accessorRole = "Hospital",
                    action = "REVOKED",
                    timestamp = System.currentTimeMillis() - 24 * 3600 * 1000L,
                    status = "Revoked",
                    verificationMethod = "Patient Manual Revocation"
                )
            )
        }
    }

    fun selectDocument(doc: MedicalDocument?) {
        _selectedDocument.value = doc
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun saveDocument(
        fileName: String,
        fileType: String,
        category: String = "Prescription",
        hospitalOrDoctor: String = "",
        recordDate: String = "",
        sourceUri: Uri?,
        description: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            val context = getApplication<Application>()
            var localPath = ""

            if (sourceUri != null) {
                val ext = when (fileType.uppercase()) {
                    "PDF" -> "pdf"
                    "PNG" -> "png"
                    "JPEG" -> "jpeg"
                    else -> "jpg"
                }
                val savedResult = DocumentStorageHelper.saveFileToPrivateStorage(context, sourceUri, ext)
                if (savedResult != null) {
                    localPath = savedResult.second
                }
            }

            val docId = "doc_" + UUID.randomUUID().toString().take(8)
            val formattedDate = recordDate.ifBlank {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            }

            val doc = MedicalDocument(
                id = docId,
                userId = "patient_user",
                fileName = fileName.ifBlank { "$category $formattedDate" },
                fileType = fileType.uppercase(),
                localFilePath = localPath,
                description = description,
                category = category,
                hospitalOrDoctor = hospitalOrDoctor.ifBlank { "Private Consultation" },
                recordDate = formattedDate,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertDocument(doc)

            // Log action to audit activity log
            repository.insertLog(
                AccessLogEntity(
                    id = UUID.randomUUID().toString(),
                    shareId = "",
                    documentTitle = doc.fileName,
                    accessorName = "Self (Patient)",
                    accessorRole = "Patient",
                    action = "RECORD_ADDED",
                    timestamp = System.currentTimeMillis(),
                    status = "Encrypted On-Device",
                    verificationMethod = "Vault Key"
                )
            )

            _userMessage.value = "$category saved securely to health vault."
            _isProcessing.value = false
            onSuccess()
        }
    }

    fun shareRecord(
        document: MedicalDocument,
        recipientType: String,
        recipientName: String,
        duration: String,
        purpose: String = "Clinical Consultation",
        onSuccess: (RecordShareEntity) -> Unit = {}
    ) {
        shareMultipleRecords(
            documents = listOf(document),
            recipientType = recipientType,
            recipientName = recipientName,
            duration = duration,
            purpose = purpose,
            onSuccess = onSuccess
        )
    }

    fun shareMultipleRecords(
        documents: List<MedicalDocument>,
        recipientType: String,
        recipientName: String,
        duration: String,
        purpose: String = "Clinical Consultation",
        onSuccess: (RecordShareEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (documents.isEmpty()) return@launch

            val now = System.currentTimeMillis()
            val durationMs = when (duration) {
                "1 Hour" -> 3600 * 1000L
                "24 Hours" -> 24 * 3600 * 1000L
                "7 Days" -> 7 * 24 * 3600 * 1000L
                "30 Days" -> 30 * 24 * 3600 * 1000L
                else -> 365L * 24 * 3600 * 1000L
            }
            val expires = if (duration == "Until Revoked") Long.MAX_VALUE else (now + durationMs)
            val tokenCode = "MB-" + (1000 + (Math.random() * 9000).toInt()) + "-" + recipientType.take(3).uppercase()

            val docNames = documents.joinToString(", ") { it.fileName }
            val docIdsJson = "[" + documents.joinToString(",") { "\"${it.id}\"" } + "]"

            val share = RecordShareEntity(
                id = UUID.randomUUID().toString(),
                documentId = documents.first().id,
                documentIdsJson = docIdsJson,
                documentName = docNames,
                documentCategory = documents.first().category,
                recipientType = recipientType,
                recipientName = recipientName.ifBlank { "Authorized Provider" },
                accessDuration = duration,
                sharedAt = now,
                expiresAt = expires,
                status = "ACTIVE",
                secureToken = tokenCode,
                purpose = purpose
            )
            repository.insertShare(share)

            // Log consent grant action
            repository.insertLog(
                AccessLogEntity(
                    id = UUID.randomUUID().toString(),
                    shareId = share.id,
                    documentTitle = docNames,
                    accessorName = share.recipientName,
                    accessorRole = recipientType,
                    action = "RECORD_SHARED",
                    timestamp = now,
                    status = "Authorized ($duration)",
                    verificationMethod = "Time-Limited QR / Code"
                )
            )

            _userMessage.value = "Access granted to $recipientName for $duration."
            onSuccess(share)
        }
    }

    fun logDocumentAccess(doc: MedicalDocument) {
        viewModelScope.launch {
            repository.insertLog(
                AccessLogEntity(
                    id = UUID.randomUUID().toString(),
                    shareId = "",
                    documentTitle = doc.fileName,
                    accessorName = "Self (Patient)",
                    accessorRole = "Patient",
                    action = "RECORD_ACCESSED",
                    timestamp = System.currentTimeMillis(),
                    status = "Verified Access",
                    verificationMethod = "Vault Key"
                )
            )
        }
    }

    fun updateShareDuration(shareId: String, newDuration: String, recipientName: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val durationMs = when (newDuration) {
                "1 Hour" -> 3600 * 1000L
                "24 Hours" -> 24 * 3600 * 1000L
                "7 Days" -> 7 * 24 * 3600 * 1000L
                "30 Days" -> 30 * 24 * 3600 * 1000L
                else -> 365L * 24 * 3600 * 1000L
            }
            val expires = if (newDuration == "Until Revoked") Long.MAX_VALUE else (now + durationMs)
            repository.updateShareDuration(shareId, newDuration, expires)
            _userMessage.value = "Access duration updated to $newDuration for $recipientName."
        }
    }

    fun revokeShare(shareId: String, recipientName: String = "provider") {
        viewModelScope.launch {
            val share = allShares.value.find { it.id == shareId } ?: activeShares.value.find { it.id == shareId }
            val docTitle = share?.documentName ?: "Health Record"
            val targetName = share?.recipientName ?: recipientName
            val role = share?.recipientType ?: "Doctor/Hospital"

            repository.revokeShare(shareId)

            // Record revoke action in audit log
            repository.insertLog(
                AccessLogEntity(
                    id = UUID.randomUUID().toString(),
                    shareId = shareId,
                    documentTitle = docTitle,
                    accessorName = targetName,
                    accessorRole = role,
                    action = "ACCESS_REVOKED",
                    timestamp = System.currentTimeMillis(),
                    status = "Access Terminated",
                    verificationMethod = "Patient Manual Revoke"
                )
            )

            _userMessage.value = "Access revoked immediately for $targetName."
        }
    }

    fun updateDocument(doc: MedicalDocument, newFileName: String, newDescription: String, newHospitalDoctor: String) {
        viewModelScope.launch {
            val updated = doc.copy(
                fileName = newFileName.ifBlank { doc.fileName },
                description = newDescription,
                hospitalOrDoctor = newHospitalDoctor.ifBlank { doc.hospitalOrDoctor },
                updatedAt = System.currentTimeMillis()
            )
            repository.updateDocument(updated)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = updated
            }
            _userMessage.value = "Record details updated."
        }
    }

    fun deleteDocument(doc: MedicalDocument) {
        viewModelScope.launch {
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

            repository.insertLog(
                AccessLogEntity(
                    id = UUID.randomUUID().toString(),
                    shareId = "",
                    documentTitle = doc.fileName,
                    accessorName = "Self (Patient)",
                    accessorRole = "Patient",
                    action = "RECORD_DELETED",
                    timestamp = System.currentTimeMillis(),
                    status = "Permanently Removed",
                    verificationMethod = "Storage Cleanup"
                )
            )

            _userMessage.value = "Record and files deleted permanently."
        }
    }

    fun updateEmergencyHealthInfo(info: EmergencyHealthInfoEntity) {
        viewModelScope.launch {
            repository.saveEmergencyInfo(info)
            _userMessage.value = "Emergency health information updated."
        }
    }

    fun toggleEmergencySharing(allowed: Boolean) {
        viewModelScope.launch {
            val current = emergencyInfo.value ?: EmergencyHealthInfoEntity()
            val updated = current.copy(isSharingAllowedInEmergency = allowed)
            repository.saveEmergencyInfo(updated)
            _userMessage.value = if (allowed) "Emergency health sharing enabled." else "Emergency health sharing disabled."
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

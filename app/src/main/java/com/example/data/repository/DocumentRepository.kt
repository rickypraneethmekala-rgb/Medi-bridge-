package com.example.data.repository

import com.example.core.storage.DocumentStorageHelper
import com.example.data.local.dao.MedicalDocumentDao
import com.example.data.local.dao.MedicineReminderDao
import com.example.data.local.entity.MedicalDocument
import com.example.data.local.entity.MedicineReminder
import kotlinx.coroutines.flow.Flow

class DocumentRepository(
    private val documentDao: MedicalDocumentDao,
    private val reminderDao: MedicineReminderDao
) {
    val allDocuments: Flow<List<MedicalDocument>> = documentDao.getAllDocuments()
    val allReminders: Flow<List<MedicineReminder>> = reminderDao.getAllReminders()

    fun getRemindersForDocument(documentId: String): Flow<List<MedicineReminder>> {
        return reminderDao.getRemindersForDocument(documentId)
    }

    suspend fun getDocumentById(id: String): MedicalDocument? {
        return documentDao.getDocumentById(id)
    }

    suspend fun insertDocument(document: MedicalDocument) {
        documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: MedicalDocument) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(document: MedicalDocument) {
        // Delete actual local file to prevent orphaned files
        DocumentStorageHelper.deletePrivateFile(document.localFilePath)
        // Delete document from Room database (cascades to reminders)
        documentDao.deleteDocument(document)
    }

    suspend fun insertReminder(reminder: MedicineReminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: MedicineReminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: MedicineReminder) {
        reminderDao.deleteReminder(reminder)
    }
}

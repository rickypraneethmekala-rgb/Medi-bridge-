package com.example.data.repository

import com.example.core.storage.DocumentStorageHelper
import com.example.data.local.dao.AccessLogDao
import com.example.data.local.dao.EmergencyHealthInfoDao
import com.example.data.local.dao.MedicalDocumentDao
import com.example.data.local.dao.MedicineReminderDao
import com.example.data.local.dao.RecordShareDao
import com.example.data.local.entity.AccessLogEntity
import com.example.data.local.entity.EmergencyHealthInfoEntity
import com.example.data.local.entity.MedicalDocument
import com.example.data.local.entity.MedicineReminder
import com.example.data.local.entity.RecordShareEntity
import kotlinx.coroutines.flow.Flow

class DocumentRepository(
    private val documentDao: MedicalDocumentDao,
    private val reminderDao: MedicineReminderDao,
    private val shareDao: RecordShareDao,
    private val logDao: AccessLogDao,
    private val emergencyDao: EmergencyHealthInfoDao
) {
    val allDocuments: Flow<List<MedicalDocument>> = documentDao.getAllDocuments()
    val allReminders: Flow<List<MedicineReminder>> = reminderDao.getAllReminders()
    val allShares: Flow<List<RecordShareEntity>> = shareDao.getAllShares()
    val activeShares: Flow<List<RecordShareEntity>> = shareDao.getActiveShares()
    val allLogs: Flow<List<AccessLogEntity>> = logDao.getAllLogs()
    val emergencyInfo: Flow<EmergencyHealthInfoEntity?> = emergencyDao.getEmergencyInfo()

    fun getRemindersForDocument(documentId: String): Flow<List<MedicineReminder>> {
        return reminderDao.getRemindersForDocument(documentId)
    }

    fun getActiveSharesForDocument(documentId: String): Flow<List<RecordShareEntity>> {
        return shareDao.getActiveSharesForDocument(documentId)
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
        // Revoke active shares for this document
        shareDao.revokeAllForDocument(document.id)
        // Delete document from Room database (cascades to reminders)
        documentDao.deleteDocument(document)
    }

    suspend fun insertShare(share: RecordShareEntity) {
        shareDao.insertShare(share)
    }

    suspend fun revokeShare(shareId: String) {
        shareDao.revokeShare(shareId)
    }

    suspend fun updateShareDuration(shareId: String, duration: String, expiresAt: Long) {
        shareDao.updateDuration(shareId, duration, expiresAt)
    }

    suspend fun insertLog(log: AccessLogEntity) {
        logDao.insertLog(log)
    }

    suspend fun saveEmergencyInfo(info: EmergencyHealthInfoEntity) {
        emergencyDao.saveEmergencyInfo(info)
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

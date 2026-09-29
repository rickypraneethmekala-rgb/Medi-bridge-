package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CachedPrescriptionEntity
import com.example.data.local.entity.LocalFamilyMemberEntity
import com.example.data.local.entity.LocalReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM local_reminders")
    fun getAllReminders(): Flow<List<LocalReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: LocalReminderEntity)

    @Update
    suspend fun updateReminder(reminder: LocalReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: LocalReminderEntity)
}

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM local_family_members")
    fun getAllFamilyMembers(): Flow<List<LocalFamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: LocalFamilyMemberEntity)

    @Delete
    suspend fun deleteFamilyMember(member: LocalFamilyMemberEntity)
}

@Dao
interface CachedPrescriptionDao {
    @Query("SELECT * FROM cached_prescriptions")
    fun getAllCached(): Flow<List<CachedPrescriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: CachedPrescriptionEntity)
}

@Dao
interface MedicalDocumentDao {
    @Query("SELECT * FROM medical_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<com.example.data.local.entity.MedicalDocument>>

    @Query("SELECT * FROM medical_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): com.example.data.local.entity.MedicalDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: com.example.data.local.entity.MedicalDocument)

    @Update
    suspend fun updateDocument(document: com.example.data.local.entity.MedicalDocument)

    @Delete
    suspend fun deleteDocument(document: com.example.data.local.entity.MedicalDocument)

    @Query("DELETE FROM medical_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)
}

@Dao
interface MedicineReminderDao {
    @Query("SELECT * FROM medicine_reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<com.example.data.local.entity.MedicineReminder>>

    @Query("SELECT * FROM medicine_reminders WHERE documentId = :documentId ORDER BY createdAt DESC")
    fun getRemindersForDocument(documentId: String): Flow<List<com.example.data.local.entity.MedicineReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: com.example.data.local.entity.MedicineReminder)

    @Update
    suspend fun updateReminder(reminder: com.example.data.local.entity.MedicineReminder)

    @Delete
    suspend fun deleteReminder(reminder: com.example.data.local.entity.MedicineReminder)

    @Query("DELETE FROM medicine_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: String)

    @Query("DELETE FROM medicine_reminders WHERE documentId = :documentId")
    suspend fun deleteRemindersByDocumentId(documentId: String)
}

@Dao
interface RecordShareDao {
    @Query("SELECT * FROM record_shares ORDER BY sharedAt DESC")
    fun getAllShares(): Flow<List<com.example.data.local.entity.RecordShareEntity>>

    @Query("SELECT * FROM record_shares WHERE status = 'ACTIVE' ORDER BY sharedAt DESC")
    fun getActiveShares(): Flow<List<com.example.data.local.entity.RecordShareEntity>>

    @Query("SELECT * FROM record_shares WHERE (documentId = :documentId OR documentIdsJson LIKE '%' || :documentId || '%') AND status = 'ACTIVE'")
    fun getActiveSharesForDocument(documentId: String): Flow<List<com.example.data.local.entity.RecordShareEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShare(share: com.example.data.local.entity.RecordShareEntity)

    @Update
    suspend fun updateShare(share: com.example.data.local.entity.RecordShareEntity)

    @Query("UPDATE record_shares SET accessDuration = :duration, expiresAt = :expiresAt WHERE id = :id")
    suspend fun updateDuration(id: String, duration: String, expiresAt: Long)

    @Query("UPDATE record_shares SET status = 'REVOKED' WHERE id = :id")
    suspend fun revokeShare(id: String)

    @Query("UPDATE record_shares SET status = 'REVOKED' WHERE documentId = :documentId OR documentIdsJson LIKE '%' || :documentId || '%'")
    suspend fun revokeAllForDocument(documentId: String)
}

@Dao
interface AccessLogDao {
    @Query("SELECT * FROM access_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<com.example.data.local.entity.AccessLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: com.example.data.local.entity.AccessLogEntity)
}

@Dao
interface EmergencyHealthInfoDao {
    @Query("SELECT * FROM emergency_health_info WHERE id = 'primary_emergency_info' LIMIT 1")
    fun getEmergencyInfo(): Flow<com.example.data.local.entity.EmergencyHealthInfoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEmergencyInfo(info: com.example.data.local.entity.EmergencyHealthInfoEntity)
}

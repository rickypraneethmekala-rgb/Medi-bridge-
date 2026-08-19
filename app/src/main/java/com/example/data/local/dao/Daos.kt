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

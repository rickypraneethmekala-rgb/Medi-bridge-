package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_reminders")
data class LocalReminderEntity(
    @PrimaryKey val id: String,
    val medicineName: String,
    val dosage: String,
    val reminderTimesJson: String,
    val totalDays: Int,
    val daysRemaining: Int,
    val isTakenToday: Boolean,
    val lastTakenTimestamp: Long = 0L
)

@Entity(tableName = "local_family_members")
data class LocalFamilyMemberEntity(
    @PrimaryKey val id: String,
    val name: String,
    val relation: String,
    val age: Int,
    val emergencyContact: String,
    val hasConsentGiven: Boolean = true
)

@Entity(tableName = "cached_prescriptions")
data class CachedPrescriptionEntity(
    @PrimaryKey val id: String,
    val doctorName: String,
    val hospitalName: String,
    val issuedDate: String,
    val medicinesJson: String,
    val isVerified: Boolean
)

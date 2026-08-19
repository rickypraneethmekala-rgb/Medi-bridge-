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

@Entity(tableName = "medical_documents")
data class MedicalDocument(
    @PrimaryKey val id: String,
    val userId: String = "local_patient",
    val fileName: String,
    val fileType: String, // "JPG", "JPEG", "PNG", "PDF"
    val localFilePath: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "medicine_reminders",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = MedicalDocument::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["documentId"])]
)
data class MedicineReminder(
    @PrimaryKey val id: String,
    val documentId: String? = null,
    val medicineName: String,
    val time: String, // e.g. "8:00 AM"
    val instruction: String = "", // e.g. "Take after breakfast"
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

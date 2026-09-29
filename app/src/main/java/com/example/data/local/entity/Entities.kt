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
    val category: String = "Prescription", // "Prescription", "Lab Report", "Discharge Summary", "Medical Bill", "Other Medical Record"
    val hospitalOrDoctor: String = "",
    val recordDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "record_shares")
data class RecordShareEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val documentIdsJson: String = "", // JSON array of record IDs when multiple records are shared
    val documentName: String,
    val documentCategory: String = "Prescription",
    val recipientType: String, // "Doctor", "Hospital", "Pharmacist", "Authorized Caregiver"
    val recipientName: String,
    val accessDuration: String, // "1 Hour", "24 Hours", "7 Days", "30 Days", "Until Revoked"
    val sharedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000L,
    val status: String = "ACTIVE", // "ACTIVE", "REVOKED", "EXPIRED"
    val secureToken: String = "", // e.g. "MB-SEC-8921"
    val purpose: String = "Clinical Consultation"
)

@Entity(tableName = "access_logs")
data class AccessLogEntity(
    @PrimaryKey val id: String,
    val shareId: String,
    val documentTitle: String,
    val accessorName: String,
    val accessorRole: String, // "Doctor", "Hospital", "Pharmacist", "Authorized Caregiver"
    val action: String, // "ACCESSED", "DOWNLOADED", "REVOKED", "GRANTED"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Authorized",
    val verificationMethod: String = "Time-Limited QR / Code"
)

@Entity(tableName = "emergency_health_info")
data class EmergencyHealthInfoEntity(
    @PrimaryKey val id: String = "primary_emergency_info",
    val bloodGroup: String = "O+",
    val allergies: String = "Penicillin, Sulfa drugs",
    val chronicConditions: String = "Mild Hypertension",
    val currentMedicines: String = "Amlodipine 5mg OD, Multivitamin",
    val emergencyContactName: String = "Priya Sharma (Spouse)",
    val emergencyContactPhone: String = "+91 98765 43210",
    val isSharingAllowedInEmergency: Boolean = true
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

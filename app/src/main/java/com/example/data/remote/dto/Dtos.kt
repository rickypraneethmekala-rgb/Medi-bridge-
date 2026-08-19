package com.example.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(val email: String, val password: String, val role: String)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String,
    val licenseOrRegNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String,
    val refreshToken: String?,
    val userId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class HospitalDto(
    val id: String,
    val name: String,
    val type: String, // "GOVERNMENT" or "PRIVATE"
    val address: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double? = null,
    val departments: List<String> = emptyList(),
    val hasEmergency24x7: Boolean = true,
    val ambulanceNumber: String? = "108"
)

@JsonClass(generateAdapter = true)
data class DoctorDto(
    val id: String,
    val hospitalId: String,
    val hospitalName: String,
    val name: String,
    val specialization: String,
    val department: String,
    val experienceYears: Int,
    val nextAvailableSlot: String,
    val currentQueueToken: Int,
    val totalInQueue: Int,
    val estimatedWaitMinutes: Int,
    val consultationFee: Double,
    val isAvailableToday: Boolean = true,
    val lastUpdated: String? = null
)

@JsonClass(generateAdapter = true)
data class BookAppointmentRequest(
    val hospitalId: String,
    val doctorId: String,
    val department: String,
    val slotDateTime: String,
    val patientName: String,
    val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class AppointmentDto(
    val appointmentId: String,
    val tokenNumber: Int,
    val hospitalName: String,
    val doctorName: String,
    val department: String,
    val slotDateTime: String,
    val status: String, // "BOOKED", "COMPLETED", "CANCELLED", "IN_CONSULTATION"
    val estimatedConsultationTime: String? = null
)

@JsonClass(generateAdapter = true)
data class QueueStatusDto(
    val doctorId: String,
    val doctorName: String,
    val currentServingToken: Int,
    val patientToken: Int,
    val patientsAhead: Int,
    val estimatedWaitMinutes: Int,
    val queueStatus: String, // "RUNNING", "PAUSED", "COMPLETED"
    val lastUpdated: String
)

@JsonClass(generateAdapter = true)
data class PrescriptionMedicineDto(
    val medicineName: String,
    val strength: String,
    val dosage: String,
    val frequency: String,
    val durationDays: Int,
    val instructions: String,
    val precautions: String? = null,
    val purpose: String? = null,
    val confidence: Float = 1.0f
)

@JsonClass(generateAdapter = true)
data class PrescriptionDto(
    val id: String,
    val doctorName: String,
    val hospitalName: String,
    val issuedDate: String,
    val diagnosisSummary: String,
    val isVerifiedByPharmacist: Boolean = false,
    val verifiedPharmacistName: String? = null,
    val medicines: List<PrescriptionMedicineDto> = emptyList(),
    val rawImageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class OcrExtractionResponse(
    val prescriptionId: String,
    val medicines: List<PrescriptionMedicineDto>,
    val overallConfidence: Float,
    val isConfident: Boolean,
    val rawExtractedText: String,
    val disclaimer: String = "Prescription could not be confidently read without verification. Please verify with your doctor/pharmacist."
)

@JsonClass(generateAdapter = true)
data class AiExplanationResponse(
    val medicineName: String,
    val language: String,
    val generalPurpose: String,
    val doctorPrescribedDosage: String,
    val frequency: String,
    val duration: String,
    val precautions: String,
    val sideEffectsInfo: String,
    val audioExplanationText: String,
    val mandatoryDisclaimer: String = "AI-generated information is for educational purposes only. Follow your doctor's or pharmacist's instructions."
)

@JsonClass(generateAdapter = true)
data class MedicineCatalogDto(
    val id: String,
    val name: String,
    val genericName: String,
    val manufacturer: String,
    val strength: String,
    val form: String, // "Tablet", "Syrup", "Injection", "Capsule"
    val generalPurpose: String,
    val precautions: String,
    val storageInfo: String,
    val requiresPrescription: Boolean = true
)

@JsonClass(generateAdapter = true)
data class PharmacyOfferDto(
    val pharmacyId: String,
    val pharmacyName: String,
    val distanceKm: Double,
    val isInStock: Boolean,
    val price: Double,
    val estimatedDeliveryMinutes: Int,
    val isVerified: Boolean = true,
    val contactPhone: String
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    val prescriptionId: String,
    val pharmacyId: String,
    val items: List<OrderItemRequest>,
    val deliveryAddress: String,
    val paymentMethod: String
)

@JsonClass(generateAdapter = true)
data class OrderItemRequest(val medicineName: String, val quantity: Int, val unitPrice: Double)

@JsonClass(generateAdapter = true)
data class OrderDto(
    val orderId: String,
    val pharmacyName: String,
    val status: String, // "PLACED", "PHARMACY_VERIFYING", "CONFIRMED", "PACKED", "OUT_FOR_DELIVERY", "DELIVERED"
    val totalAmount: Double,
    val deliveryOtp: String,
    val deliveryPartnerName: String? = null,
    val deliveryPartnerPhone: String? = null,
    val estimatedArrival: String? = null,
    val currentLat: Double? = null,
    val currentLng: Double? = null
)

@JsonClass(generateAdapter = true)
data class MedicineReminderDto(
    val id: String,
    val medicineName: String,
    val dosage: String,
    val times: List<String>, // e.g. ["08:00 AM", "02:00 PM", "08:00 PM"]
    val totalDays: Int,
    val daysRemaining: Int,
    val status: String // "ACTIVE", "TAKEN_TODAY", "MISSED", "COMPLETED"
)

package com.example.data.remote.dto

import com.squareup.moshi.JsonClass

enum class PrescriptionOrderStatus(val label: String, val badgeColor: String) {
    PRESCRIPTION_RECEIVED("Waiting for Pharmacist", "YELLOW"),
    PHARMACIST_REVIEWING("Pharmacist Reviewing", "BLUE"),
    CLARIFICATION_REQUIRED("Clarification Required", "ORANGE"),
    CLARIFICATION_RECEIVED("Clarification Sent", "YELLOW"),
    PRESCRIPTION_VERIFIED("Prescription Verified", "GREEN"),
    PATIENT_CONFIRMED("Patient Confirmed", "BLUE"),
    PAYMENT_COMPLETED("Payment Completed", "GREEN"),
    PREPARING("Pharmacy Preparing", "BLUE"),
    OUT_FOR_DELIVERY("Out for Delivery", "PURPLE"),
    DELIVERED("Delivered", "GREEN"),
    REJECTED("Rejected", "RED")
}

@JsonClass(generateAdapter = true)
data class ClarificationMessage(
    val id: String,
    val senderRole: String, // "PHARMACIST" or "PATIENT"
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentPath: String? = null,
    val requiresNewImage: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VerifiedMedicineItem(
    val id: String,
    val medicineName: String,
    val strength: String = "Standard",
    val quantity: Int = 1,
    val form: String = "Tablet",
    val price: Double = 0.0,
    val isInStock: Boolean = true
)

@JsonClass(generateAdapter = true)
data class PrescriptionOrder(
    val id: String,
    val patientId: String = "patient_101",
    val patientName: String = "Ricky",
    val patientPhone: String = "+91 98765 43210",
    val pharmacyId: String = "pharmacy_apollo_1",
    val pharmacyName: String = "Apollo Pharmacy",
    val prescriptionFilePath: String,
    val prescriptionFileType: String, // "IMAGE" or "PDF"
    val patientNote: String = "",
    val status: PrescriptionOrderStatus = PrescriptionOrderStatus.PRESCRIPTION_RECEIVED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val clarifications: List<ClarificationMessage> = emptyList(),
    val verifiedMedicines: List<VerifiedMedicineItem> = emptyList(),
    val deliveryFee: Double = 40.0,
    val totalAmount: Double = 0.0,
    val estimatedDeliveryMinutes: Int = 30,
    val deliveryAddress: String = "Flat 402, Green Valley Apartments, Raidurg",
    val deliveryPartnerName: String? = null,
    val deliveryOtp: String = "4829",
    val rejectionReason: String? = null,
    val paymentMethod: String? = null,
    val paymentTransactionId: String? = null
)

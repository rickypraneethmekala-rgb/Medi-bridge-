package com.example.data.repository

import android.content.Context
import com.example.core.notification.LocalNotificationHelper
import com.example.data.remote.dto.ClarificationMessage
import com.example.data.remote.dto.PrescriptionOrder
import com.example.data.remote.dto.PrescriptionOrderStatus
import com.example.data.remote.dto.VerifiedMedicineItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class PrescriptionOrderRepository private constructor(private val context: Context) {

    private val _orders = MutableStateFlow<List<PrescriptionOrder>>(emptyList())
    val orders: StateFlow<List<PrescriptionOrder>> = _orders.asStateFlow()

    init {
        // Seed initial demo prescription request for seamless testing
        val initialDemoOrder = PrescriptionOrder(
            id = "RX_REQ_8402",
            patientId = "patient_101",
            patientName = "Ricky",
            patientPhone = "+91 98765 43210",
            pharmacyId = "pharmacy_apollo_1",
            pharmacyName = "Apollo Pharmacy",
            prescriptionFilePath = "",
            prescriptionFileType = "IMAGE",
            patientNote = "Please provide all medicines mentioned in this prescription.",
            status = PrescriptionOrderStatus.PRESCRIPTION_RECEIVED,
            createdAt = System.currentTimeMillis() - 600000,
            updatedAt = System.currentTimeMillis() - 600000,
            deliveryFee = 40.0,
            totalAmount = 0.0,
            deliveryOtp = "5821"
        )
        _orders.value = listOf(initialDemoOrder)
    }

    companion object {
        @Volatile
        private var INSTANCE: PrescriptionOrderRepository? = null

        fun getInstance(context: Context): PrescriptionOrderRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = PrescriptionOrderRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun getOrderById(orderId: String): PrescriptionOrder? {
        return _orders.value.find { it.id == orderId }
    }

    fun createPrescriptionRequest(
        patientName: String,
        patientPhone: String,
        pharmacyId: String,
        pharmacyName: String,
        filePath: String,
        fileType: String,
        patientNote: String
    ): PrescriptionOrder {
        val randomNum = (1000 + (Math.random() * 9000).toInt())
        val randomOtp = (1000 + (Math.random() * 9000).toInt()).toString()
        val order = PrescriptionOrder(
            id = "RX_REQ_$randomNum",
            patientId = "patient_user",
            patientName = patientName.ifBlank { "Ricky" },
            patientPhone = patientPhone.ifBlank { "+91 98765 43210" },
            pharmacyId = pharmacyId.ifBlank { "pharmacy_apollo_1" },
            pharmacyName = pharmacyName.ifBlank { "Apollo Pharmacy" },
            prescriptionFilePath = filePath,
            prescriptionFileType = fileType,
            patientNote = patientNote,
            status = PrescriptionOrderStatus.PRESCRIPTION_RECEIVED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deliveryFee = 40.0,
            totalAmount = 0.0,
            deliveryOtp = randomOtp
        )

        _orders.value = listOf(order) + _orders.value

        LocalNotificationHelper.showPharmacyNotification(
            context = context,
            title = "Prescription Sent to Pharmacist",
            message = "Prescription Request #${order.id} sent to ${order.pharmacyName}. Waiting for review."
        )

        return order
    }

    fun pharmacistStartReview(orderId: String) {
        updateOrder(orderId) { it.copy(status = PrescriptionOrderStatus.PHARMACIST_REVIEWING) }
    }

    fun pharmacistRequestClarification(orderId: String, reason: String, requiresNewImage: Boolean) {
        val order = getOrderById(orderId) ?: return
        val message = ClarificationMessage(
            id = UUID.randomUUID().toString(),
            senderRole = "PHARMACIST",
            senderName = "Pharmacist (${order.pharmacyName})",
            message = reason.ifBlank { "Please upload a clearer image of the prescription or confirm medicine strength." },
            timestamp = System.currentTimeMillis(),
            requiresNewImage = requiresNewImage
        )

        updateOrder(orderId) {
            it.copy(
                status = PrescriptionOrderStatus.CLARIFICATION_REQUIRED,
                clarifications = it.clarifications + message
            )
        }

        LocalNotificationHelper.showPharmacyNotification(
            context = context,
            title = "Pharmacist Requested Clarification",
            message = "Pharmacist message: '${message.message}' for Request #${order.id}."
        )
    }

    fun patientSubmitClarification(orderId: String, replyMessage: String, newAttachmentPath: String?) {
        val order = getOrderById(orderId) ?: return
        val message = ClarificationMessage(
            id = UUID.randomUUID().toString(),
            senderRole = "PATIENT",
            senderName = order.patientName,
            message = replyMessage.ifBlank { "Updated prescription image / note uploaded." },
            timestamp = System.currentTimeMillis(),
            attachmentPath = newAttachmentPath
        )

        updateOrder(orderId) {
            it.copy(
                status = PrescriptionOrderStatus.CLARIFICATION_RECEIVED,
                clarifications = it.clarifications + message,
                prescriptionFilePath = newAttachmentPath ?: it.prescriptionFilePath
            )
        }

        LocalNotificationHelper.showPharmacyNotification(
            context = context,
            title = "Clarification Sent to Pharmacist",
            message = "Your response for Request #${order.id} has been submitted to the pharmacist."
        )
    }

    fun pharmacistVerifyPrescription(orderId: String, medicines: List<VerifiedMedicineItem>) {
        val order = getOrderById(orderId) ?: return
        val medsTotal = medicines.filter { it.isInStock }.sumOf { it.price * it.quantity }
        val grandTotal = medsTotal + order.deliveryFee

        updateOrder(orderId) {
            it.copy(
                status = PrescriptionOrderStatus.PRESCRIPTION_VERIFIED,
                verifiedMedicines = medicines,
                totalAmount = grandTotal
            )
        }

        LocalNotificationHelper.showPharmacyNotification(
            context = context,
            title = "Prescription Verified",
            message = "Your prescription #${order.id} has been verified by ${order.pharmacyName}. Total: ₹${grandTotal.toInt()}."
        )
    }

    fun pharmacistRejectPrescription(orderId: String, reason: String) {
        val order = getOrderById(orderId) ?: return
        val cleanReason = reason.ifBlank { "Prescription information is incomplete or unreadable." }

        updateOrder(orderId) {
            it.copy(
                status = PrescriptionOrderStatus.REJECTED,
                rejectionReason = cleanReason
            )
        }

        LocalNotificationHelper.showPharmacyNotification(
            context = context,
            title = "Prescription Request Rejected",
            message = "Prescription #${order.id} was rejected: $cleanReason"
        )
    }

    fun patientConfirmAndPay(orderId: String, paymentMethod: String): Boolean {
        val order = getOrderById(orderId) ?: return false
        if (order.status != PrescriptionOrderStatus.PRESCRIPTION_VERIFIED) return false

        val txnId = "TXN_" + System.currentTimeMillis().toString().takeLast(6)
        updateOrder(orderId) {
            it.copy(
                status = PrescriptionOrderStatus.PAYMENT_COMPLETED,
                paymentMethod = paymentMethod,
                paymentTransactionId = txnId
            )
        }

        LocalNotificationHelper.showPharmacyNotification(
            context = context,
            title = "Payment Successful & Order Placed",
            message = "Order #${order.id} confirmed. Pharmacy is preparing your medicines."
        )

        return true
    }

    fun pharmacyPrepareOrder(orderId: String) {
        updateOrder(orderId) { it.copy(status = PrescriptionOrderStatus.PREPARING) }

        LocalNotificationHelper.showPharmacyNotification(
            context = context,
            title = "Order Being Prepared",
            message = "Order #${orderId} is being packed at the pharmacy."
        )
    }

    fun deliveryDispatch(orderId: String, partnerName: String = "Ramesh Kumar (MediBridge Express)") {
        val order = getOrderById(orderId) ?: return
        updateOrder(orderId) {
            it.copy(
                status = PrescriptionOrderStatus.OUT_FOR_DELIVERY,
                deliveryPartnerName = partnerName,
                estimatedDeliveryMinutes = 20
            )
        }

        LocalNotificationHelper.showPharmacyNotification(
            context = context,
            title = "Order Out for Delivery",
            message = "Order #${order.id} is on the way with $partnerName. Delivery OTP: ${order.deliveryOtp}."
        )
    }

    fun verifyDeliveryOtp(orderId: String, enteredOtp: String): Boolean {
        val order = getOrderById(orderId) ?: return false
        if (order.deliveryOtp == enteredOtp.trim()) {
            updateOrder(orderId) { it.copy(status = PrescriptionOrderStatus.DELIVERED) }

            LocalNotificationHelper.showPharmacyNotification(
                context = context,
                title = "Order Delivered",
                message = "Order #${order.id} has been delivered successfully. Thank you!"
            )
            return true
        }
        return false
    }

    private fun updateOrder(orderId: String, transform: (PrescriptionOrder) -> PrescriptionOrder) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) {
                transform(it).copy(updatedAt = System.currentTimeMillis())
            } else it
        }
    }
}

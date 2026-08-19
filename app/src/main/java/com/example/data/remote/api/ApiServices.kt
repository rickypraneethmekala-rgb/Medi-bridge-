package com.example.data.remote.api

import com.example.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Query("email") email: String): Response<Map<String, String>>
}

interface HospitalApi {
    @GET("hospitals/nearby")
    suspend fun getNearbyHospitals(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("type") type: String? = null // "GOVERNMENT", "PRIVATE", or null for all
    ): Response<List<HospitalDto>>

    @GET("hospitals/{id}")
    suspend fun getHospitalDetails(@Path("id") id: String): Response<HospitalDto>

    @GET("hospitals/{id}/departments")
    suspend fun getDepartments(@Path("id") hospitalId: String): Response<List<String>>
}

interface DoctorApi {
    @GET("doctors")
    suspend fun getDoctors(
        @Query("hospitalId") hospitalId: String? = null,
        @Query("department") department: String? = null
    ): Response<List<DoctorDto>>

    @GET("doctors/{id}")
    suspend fun getDoctorDetails(@Path("id") doctorId: String): Response<DoctorDto>
}

interface AppointmentApi {
    @POST("appointments/book")
    suspend fun bookAppointment(@Body request: BookAppointmentRequest): Response<AppointmentDto>

    @GET("appointments/patient/{patientId}")
    suspend fun getPatientAppointments(@Path("patientId") patientId: String): Response<List<AppointmentDto>>

    @POST("appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") appointmentId: String): Response<Map<String, Boolean>>

    @POST("appointments/{id}/reschedule")
    suspend fun rescheduleAppointment(
        @Path("id") appointmentId: String,
        @Query("newSlot") newSlot: String
    ): Response<AppointmentDto>
}

interface QueueApi {
    @GET("queue/doctor/{doctorId}/live")
    suspend fun getLiveDoctorQueue(@Path("doctorId") doctorId: String): Response<QueueStatusDto>

    @GET("queue/appointment/{appointmentId}")
    suspend fun getAppointmentQueueToken(@Path("appointmentId") appointmentId: String): Response<QueueStatusDto>
}

interface PrescriptionApi {
    @GET("prescriptions/patient/{patientId}")
    suspend fun getPrescriptions(@Path("patientId") patientId: String): Response<List<PrescriptionDto>>

    @Multipart
    @POST("prescriptions/upload")
    suspend fun uploadPrescriptionImage(@Part image: MultipartBody.Part): Response<PrescriptionDto>
}

interface OCRApi {
    @POST("ocr/extract")
    suspend fun extractPrescriptionOcr(@Query("imageUrl") imageUrl: String): Response<OcrExtractionResponse>
}

interface AIHealthApi {
    @GET("ai/explain-prescription")
    suspend fun explainPrescription(
        @Query("medicineName") medicineName: String,
        @Query("dosage") dosage: String,
        @Query("language") language: String // "en", "te", "hi"
    ): Response<AiExplanationResponse>

    @GET("ai/health-assistant")
    suspend fun askHealthQuestion(
        @Query("query") query: String,
        @Query("language") language: String
    ): Response<Map<String, String>>
}

interface MedicineApi {
    @GET("medicines/search")
    suspend fun searchMedicines(@Query("q") query: String): Response<List<MedicineCatalogDto>>

    @GET("medicines/{id}")
    suspend fun getMedicineDetails(@Path("id") id: String): Response<MedicineCatalogDto>
}

interface PharmacyApi {
    @GET("pharmacies/nearby")
    suspend fun getNearbyPharmacies(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int? = 5000
    ): Response<List<PharmacyDetailsDto>>

    @GET("pharmacies/{id}")
    suspend fun getPharmacyDetails(@Path("id") id: String): Response<PharmacyDetailsDto>

    @GET("pharmacies/compare-price")
    suspend fun comparePharmaciesForMedicine(
        @Query("medicineName") medicineName: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<List<PharmacyOfferDto>>

    @POST("pharmacies/prescriptions/{id}/verify")
    suspend fun verifyPrescription(
        @Path("id") prescriptionId: String,
        @Query("isApproved") isApproved: Boolean,
        @Query("notes") notes: String
    ): Response<Map<String, Boolean>>
}

interface OrderApi {
    @POST("orders/create")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderDto>

    @GET("orders/{orderId}/status")
    suspend fun getOrderStatus(@Path("orderId") orderId: String): Response<OrderDto>

    @GET("orders/patient/{patientId}")
    suspend fun getPatientOrders(@Path("patientId") patientId: String): Response<List<OrderDto>>
}

interface DeliveryApi {
    @GET("delivery/{orderId}/tracking")
    suspend fun trackDelivery(@Path("orderId") orderId: String): Response<OrderDto>

    @POST("delivery/{orderId}/verify-otp")
    suspend fun verifyDeliveryOtp(
        @Path("orderId") orderId: String,
        @Query("otp") otp: String
    ): Response<Map<String, Boolean>>
}

interface ReminderApi {
    @GET("reminders/patient/{patientId}")
    suspend fun getReminders(@Path("patientId") patientId: String): Response<List<MedicineReminderDto>>

    @POST("reminders/{id}/log")
    suspend fun logDoseAction(
        @Path("id") reminderId: String,
        @Query("action") action: String // "TAKEN", "MISSED"
    ): Response<Map<String, Boolean>>
}

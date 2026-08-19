package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.config.ApiConfig
import com.example.core.network.ApiResult
import com.example.core.network.RetrofitClientProvider
import com.example.data.remote.api.DeliveryApi
import com.example.data.remote.api.OrderApi
import com.example.data.remote.dto.CreateOrderRequest
import com.example.data.remote.dto.OrderDto
import com.example.data.remote.dto.OrderItemRequest
import com.example.data.remote.dto.PrescriptionOrder
import com.example.data.remote.dto.VerifiedMedicineItem
import com.example.data.repository.PrescriptionOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderDeliveryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PrescriptionOrderRepository.getInstance(application)

    val prescriptionOrders: StateFlow<List<PrescriptionOrder>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _orderState = MutableStateFlow<ApiResult<OrderDto>?>(null)
    val orderState: StateFlow<ApiResult<OrderDto>?> = _orderState.asStateFlow()

    private val _trackingState = MutableStateFlow<ApiResult<OrderDto>>(ApiResult.Unconfigured("Delivery Tracking", "DELIVERY_API_URL"))
    val trackingState: StateFlow<ApiResult<OrderDto>> = _trackingState.asStateFlow()

    private val _otpVerificationState = MutableStateFlow<ApiResult<Boolean>?>(null)
    val otpVerificationState: StateFlow<ApiResult<Boolean>?> = _otpVerificationState.asStateFlow()

    fun uploadPrescriptionRequest(
        patientName: String,
        patientPhone: String,
        pharmacyId: String,
        pharmacyName: String,
        filePath: String,
        fileType: String,
        patientNote: String
    ): PrescriptionOrder {
        return repository.createPrescriptionRequest(
            patientName = patientName,
            patientPhone = patientPhone,
            pharmacyId = pharmacyId,
            pharmacyName = pharmacyName,
            filePath = filePath,
            fileType = fileType,
            patientNote = patientNote
        )
    }

    fun pharmacistStartReview(orderId: String) {
        repository.pharmacistStartReview(orderId)
    }

    fun pharmacistRequestClarification(orderId: String, reason: String, requiresNewImage: Boolean) {
        repository.pharmacistRequestClarification(orderId, reason, requiresNewImage)
    }

    fun patientSubmitClarification(orderId: String, replyMessage: String, newAttachmentPath: String?) {
        repository.patientSubmitClarification(orderId, replyMessage, newAttachmentPath)
    }

    fun pharmacistVerifyPrescription(orderId: String, medicines: List<VerifiedMedicineItem>) {
        repository.pharmacistVerifyPrescription(orderId, medicines)
    }

    fun pharmacistRejectPrescription(orderId: String, reason: String) {
        repository.pharmacistRejectPrescription(orderId, reason)
    }

    fun patientConfirmAndPay(orderId: String, paymentMethod: String): Boolean {
        return repository.patientConfirmAndPay(orderId, paymentMethod)
    }

    fun pharmacyPrepareOrder(orderId: String) {
        repository.pharmacyPrepareOrder(orderId)
    }

    fun deliveryDispatch(orderId: String, partnerName: String = "Ramesh Kumar (MediBridge Express)") {
        repository.deliveryDispatch(orderId, partnerName)
    }

    fun verifyDeliveryOtpLocal(orderId: String, enteredOtp: String): Boolean {
        val result = repository.verifyDeliveryOtp(orderId, enteredOtp)
        _otpVerificationState.value = ApiResult.Success(result)
        return result
    }

    fun placeOrder(
        prescriptionId: String,
        pharmacyId: String,
        items: List<OrderItemRequest>,
        deliveryAddress: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            _orderState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<OrderApi>(context, ApiConfig.ENDPOINT_ORDER)
            if (api == null) {
                _orderState.value = ApiResult.Unconfigured("Order Processing", "ORDER_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("Order") {
                api.createOrder(
                    CreateOrderRequest(
                        prescriptionId = prescriptionId,
                        pharmacyId = pharmacyId,
                        items = items,
                        deliveryAddress = deliveryAddress,
                        paymentMethod = paymentMethod
                    )
                )
            }
            _orderState.value = result
        }
    }

    fun trackOrder(orderId: String) {
        viewModelScope.launch {
            _trackingState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<DeliveryApi>(context, ApiConfig.ENDPOINT_DELIVERY)
            if (api == null) {
                _trackingState.value = ApiResult.Unconfigured("Delivery Tracking", "DELIVERY_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("Delivery") {
                api.trackDelivery(orderId)
            }
            _trackingState.value = result
        }
    }

    fun verifyDeliveryOtp(orderId: String, enteredOtp: String) {
        viewModelScope.launch {
            _otpVerificationState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<DeliveryApi>(context, ApiConfig.ENDPOINT_DELIVERY)
            if (api == null) {
                _otpVerificationState.value = ApiResult.Unconfigured("OTP Verification", "DELIVERY_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("OTP Verification") {
                api.verifyDeliveryOtp(orderId, enteredOtp)
            }
            when (result) {
                is ApiResult.Success -> _otpVerificationState.value = ApiResult.Success(result.data["isVerified"] ?: false)
                is ApiResult.HttpError -> _otpVerificationState.value = result
                is ApiResult.NetworkError -> _otpVerificationState.value = result
                is ApiResult.Unconfigured -> _otpVerificationState.value = result
                ApiResult.Loading -> {}
            }
        }
    }
}

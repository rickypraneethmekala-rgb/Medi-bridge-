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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderDeliveryViewModel(application: Application) : AndroidViewModel(application) {
    private val _orderState = MutableStateFlow<ApiResult<OrderDto>?>(null)
    val orderState: StateFlow<ApiResult<OrderDto>?> = _orderState.asStateFlow()

    private val _trackingState = MutableStateFlow<ApiResult<OrderDto>>(ApiResult.Unconfigured("Delivery Tracking", "DELIVERY_API_URL"))
    val trackingState: StateFlow<ApiResult<OrderDto>> = _trackingState.asStateFlow()

    private val _otpVerificationState = MutableStateFlow<ApiResult<Boolean>?>(null)
    val otpVerificationState: StateFlow<ApiResult<Boolean>?> = _otpVerificationState.asStateFlow()

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

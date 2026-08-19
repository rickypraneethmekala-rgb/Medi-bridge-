package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.config.ApiConfig
import com.example.core.network.ApiResult
import com.example.core.network.RetrofitClientProvider
import com.example.core.security.SecureStorage
import com.example.data.remote.api.AppointmentApi
import com.example.data.remote.api.QueueApi
import com.example.data.remote.dto.AppointmentDto
import com.example.data.remote.dto.BookAppointmentRequest
import com.example.data.remote.dto.QueueStatusDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppointmentQueueViewModel(application: Application) : AndroidViewModel(application) {
    private val _appointmentsState = MutableStateFlow<ApiResult<List<AppointmentDto>>>(ApiResult.Unconfigured("Appointments", "APPOINTMENT_API_URL"))
    val appointmentsState: StateFlow<ApiResult<List<AppointmentDto>>> = _appointmentsState.asStateFlow()

    private val _liveQueueState = MutableStateFlow<ApiResult<QueueStatusDto>>(ApiResult.Unconfigured("Live Queue Service", "QUEUE_API_URL"))
    val liveQueueState: StateFlow<ApiResult<QueueStatusDto>> = _liveQueueState.asStateFlow()

    private val _bookingResult = MutableStateFlow<ApiResult<AppointmentDto>?>(null)
    val bookingResult: StateFlow<ApiResult<AppointmentDto>?> = _bookingResult.asStateFlow()

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        viewModelScope.launch {
            _appointmentsState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<AppointmentApi>(context, ApiConfig.ENDPOINT_APPOINTMENT)
            if (api == null) {
                _appointmentsState.value = ApiResult.Unconfigured("Appointments", "APPOINTMENT_API_URL")
                return@launch
            }

            val session = SecureStorage(context).getSession()
            val patientId = session?.id ?: "guest_patient"
            val result = RetrofitClientProvider.safeApiCall("Appointments") {
                api.getPatientAppointments(patientId)
            }
            _appointmentsState.value = result
        }
    }

    fun bookAppointment(
        hospitalId: String,
        doctorId: String,
        department: String,
        slotDateTime: String,
        patientName: String
    ) {
        viewModelScope.launch {
            _bookingResult.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<AppointmentApi>(context, ApiConfig.ENDPOINT_APPOINTMENT)
            if (api == null) {
                _bookingResult.value = ApiResult.Unconfigured("Appointment Booking", "APPOINTMENT_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("Booking") {
                api.bookAppointment(
                    BookAppointmentRequest(
                        hospitalId = hospitalId,
                        doctorId = doctorId,
                        department = department,
                        slotDateTime = slotDateTime,
                        patientName = patientName
                    )
                )
            }
            _bookingResult.value = result
            if (result is ApiResult.Success) {
                loadAppointments()
            }
        }
    }

    fun refreshLiveQueue(doctorId: String) {
        viewModelScope.launch {
            _liveQueueState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<QueueApi>(context, ApiConfig.ENDPOINT_QUEUE)
            if (api == null) {
                _liveQueueState.value = ApiResult.Unconfigured("Live Queue Tracker", "QUEUE_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("Live Queue") {
                api.getLiveDoctorQueue(doctorId)
            }
            _liveQueueState.value = result
        }
    }

    fun clearBookingState() {
        _bookingResult.value = null
    }
}

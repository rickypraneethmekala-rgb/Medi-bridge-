package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.config.ApiConfig
import com.example.core.network.ApiResult
import com.example.core.network.RetrofitClientProvider
import com.example.data.remote.api.DoctorApi
import com.example.data.remote.api.HospitalApi
import com.example.data.remote.dto.DoctorDto
import com.example.data.remote.dto.HospitalDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HospitalDoctorViewModel(application: Application) : AndroidViewModel(application) {
    private val _hospitalsState = MutableStateFlow<ApiResult<List<HospitalDto>>>(ApiResult.Unconfigured("Hospital Discovery", "HOSPITAL_API_URL"))
    val hospitalsState: StateFlow<ApiResult<List<HospitalDto>>> = _hospitalsState.asStateFlow()

    private val _doctorsState = MutableStateFlow<ApiResult<List<DoctorDto>>>(ApiResult.Unconfigured("Doctor Discovery", "DOCTOR_API_URL"))
    val doctorsState: StateFlow<ApiResult<List<DoctorDto>>> = _doctorsState.asStateFlow()

    private val _selectedHospital = MutableStateFlow<HospitalDto?>(null)
    val selectedHospital: StateFlow<HospitalDto?> = _selectedHospital.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<String?>("ALL") // "ALL", "GOVERNMENT", "PRIVATE"
    val selectedTypeFilter: StateFlow<String?> = _selectedTypeFilter.asStateFlow()

    init {
        loadHospitals()
    }

    fun setFilter(type: String) {
        _selectedTypeFilter.value = type
        loadHospitals()
    }

    fun selectHospital(hospital: HospitalDto) {
        _selectedHospital.value = hospital
        loadDoctorsForHospital(hospital.id)
    }

    fun loadHospitals() {
        viewModelScope.launch {
            _hospitalsState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<HospitalApi>(context, ApiConfig.ENDPOINT_HOSPITAL)
            if (api == null) {
                _hospitalsState.value = ApiResult.Unconfigured("Hospital Discovery", "HOSPITAL_API_URL")
                return@launch
            }

            val filterType = if (_selectedTypeFilter.value == "ALL") null else _selectedTypeFilter.value
            val result = RetrofitClientProvider.safeApiCall("Hospital") {
                api.getNearbyHospitals(lat = 17.3850, lng = 78.4867, type = filterType)
            }
            _hospitalsState.value = result
        }
    }

    fun loadDoctorsForHospital(hospitalId: String) {
        viewModelScope.launch {
            _doctorsState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<DoctorApi>(context, ApiConfig.ENDPOINT_DOCTOR)
            if (api == null) {
                _doctorsState.value = ApiResult.Unconfigured("Doctor Discovery", "DOCTOR_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("Doctor") {
                api.getDoctors(hospitalId = hospitalId)
            }
            _doctorsState.value = result
        }
    }
}

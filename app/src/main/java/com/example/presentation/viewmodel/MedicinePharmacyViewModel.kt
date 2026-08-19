package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.config.ApiConfig
import com.example.core.network.ApiResult
import com.example.core.network.RetrofitClientProvider
import com.example.data.remote.api.MedicineApi
import com.example.data.remote.api.PharmacyApi
import com.example.data.remote.dto.MedicineCatalogDto
import com.example.data.remote.dto.PharmacyOfferDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedicinePharmacyViewModel(application: Application) : AndroidViewModel(application) {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _medicineCatalogState = MutableStateFlow<ApiResult<List<MedicineCatalogDto>>>(ApiResult.Unconfigured("Medicine Catalog", "MEDICINE_API_URL"))
    val medicineCatalogState: StateFlow<ApiResult<List<MedicineCatalogDto>>> = _medicineCatalogState.asStateFlow()

    private val _pharmacyOffersState = MutableStateFlow<ApiResult<List<PharmacyOfferDto>>>(ApiResult.Unconfigured("Pharmacy Price Comparison", "PHARMACY_API_URL"))
    val pharmacyOffersState: StateFlow<ApiResult<List<PharmacyOfferDto>>> = _pharmacyOffersState.asStateFlow()

    private val _selectedMedicine = MutableStateFlow<MedicineCatalogDto?>(null)
    val selectedMedicine: StateFlow<MedicineCatalogDto?> = _selectedMedicine.asStateFlow()

    fun searchMedicines(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _medicineCatalogState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<MedicineApi>(context, ApiConfig.ENDPOINT_MEDICINE)
            if (api == null) {
                _medicineCatalogState.value = ApiResult.Unconfigured("Medicine Catalog", "MEDICINE_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("Medicine Catalog") {
                api.searchMedicines(query)
            }
            _medicineCatalogState.value = result
        }
    }

    fun comparePharmacies(medicineName: String) {
        viewModelScope.launch {
            _pharmacyOffersState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<PharmacyApi>(context, ApiConfig.ENDPOINT_PHARMACY)
            if (api == null) {
                _pharmacyOffersState.value = ApiResult.Unconfigured("Pharmacy Comparison", "PHARMACY_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("Pharmacy Comparison") {
                api.comparePharmaciesForMedicine(medicineName = medicineName, lat = 17.3850, lng = 78.4867)
            }
            _pharmacyOffersState.value = result
        }
    }

    fun selectMedicine(medicine: MedicineCatalogDto) {
        _selectedMedicine.value = medicine
        comparePharmacies(medicine.name)
    }
}

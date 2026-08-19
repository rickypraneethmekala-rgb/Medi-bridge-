package com.example.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.config.ApiConfig
import com.example.core.location.DeviceLocationManager
import com.example.core.location.UserLocationData
import com.example.core.network.ApiResult
import com.example.core.network.RetrofitClientProvider
import com.example.data.remote.api.MedicineApi
import com.example.data.remote.api.PharmacyApi
import com.example.data.remote.dto.MedicineCatalogDto
import com.example.data.remote.dto.PharmacyDetailsDto
import com.example.data.remote.dto.PharmacyMedicineItemDto
import com.example.data.remote.dto.PharmacyOfferDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PharmacySortOption {
    LOWEST_PRICE,
    NEAREST_DISTANCE,
    FASTEST_DELIVERY
}

class MedicinePharmacyViewModel(application: Application) : AndroidViewModel(application) {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userLocation = MutableStateFlow<UserLocationData?>(null)
    val userLocation: StateFlow<UserLocationData?> = _userLocation.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    private val _isGpsEnabled = MutableStateFlow(true)
    val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled.asStateFlow()

    private val _nearbyPharmaciesState = MutableStateFlow<ApiResult<List<PharmacyDetailsDto>>>(ApiResult.Loading)
    val nearbyPharmaciesState: StateFlow<ApiResult<List<PharmacyDetailsDto>>> = _nearbyPharmaciesState.asStateFlow()

    private val _selectedPharmacy = MutableStateFlow<PharmacyDetailsDto?>(null)
    val selectedPharmacy: StateFlow<PharmacyDetailsDto?> = _selectedPharmacy.asStateFlow()

    private val _medicineCatalogState = MutableStateFlow<ApiResult<List<MedicineCatalogDto>>>(ApiResult.Unconfigured("Medicine Catalog", "MEDICINE_API_URL"))
    val medicineCatalogState: StateFlow<ApiResult<List<MedicineCatalogDto>>> = _medicineCatalogState.asStateFlow()

    private val _pharmacyOffersState = MutableStateFlow<ApiResult<List<PharmacyOfferDto>>>(ApiResult.Unconfigured("Pharmacy Price Comparison", "PHARMACY_API_URL"))
    val pharmacyOffersState: StateFlow<ApiResult<List<PharmacyOfferDto>>> = _pharmacyOffersState.asStateFlow()

    private val _selectedMedicine = MutableStateFlow<MedicineCatalogDto?>(null)
    val selectedMedicine: StateFlow<MedicineCatalogDto?> = _selectedMedicine.asStateFlow()

    private val _sortOption = MutableStateFlow(PharmacySortOption.NEAREST_DISTANCE)
    val sortOption: StateFlow<PharmacySortOption> = _sortOption.asStateFlow()

    init {
        checkLocationPermission()
    }

    fun checkLocationPermission() {
        val context = getApplication<Application>()
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val granted = fineGranted || coarseGranted
        _locationPermissionGranted.value = granted

        val locManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsOn = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        _isGpsEnabled.value = gpsOn

        if (granted) {
            startLocationUpdates()
        } else {
            // Load demo fallback for unpermissioned state
            loadDemoNearbyPharmacies(localityName = "Your Area")
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        _locationPermissionGranted.value = isGranted
        if (isGranted) {
            startLocationUpdates()
        } else {
            loadDemoNearbyPharmacies(localityName = "Your Area")
        }
    }

    private fun startLocationUpdates() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            DeviceLocationManager.getLocationFlow(context).collect { loc ->
                if (loc != null) {
                    _userLocation.value = loc
                    loadNearbyPharmacies(loc.latitude, loc.longitude, loc.locality)
                } else {
                    val current = _userLocation.value
                    if (current != null) {
                        loadNearbyPharmacies(current.latitude, current.longitude, current.locality)
                    } else {
                        loadDemoNearbyPharmacies(localityName = "Your Area")
                    }
                }
            }
        }
    }

    fun loadNearbyPharmacies(lat: Double, lng: Double, localityName: String) {
        viewModelScope.launch {
            _nearbyPharmaciesState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<PharmacyApi>(context, ApiConfig.ENDPOINT_PHARMACY)
            if (api == null) {
                // Return Unconfigured with fallback demo data enabled for the detected locality
                loadDemoNearbyPharmacies(localityName)
                return@launch
            }

            try {
                val response = api.getNearbyPharmacies(lat, lng)
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!.sortedBy { it.distanceKm }
                    _nearbyPharmaciesState.value = ApiResult.Success(list)
                } else {
                    _nearbyPharmaciesState.value = ApiResult.HttpError(response.code(), "Nearby pharmacy service is temporarily unavailable.")
                }
            } catch (e: Exception) {
                _nearbyPharmaciesState.value = ApiResult.NetworkError("Nearby pharmacy service is temporarily unavailable.")
            }
        }
    }

    private fun loadDemoNearbyPharmacies(localityName: String) {
        val loc = localityName.ifBlank { "Raidurg" }
        val demoPharmacies = listOf(
            PharmacyDetailsDto(
                id = "pharmacy_apollo_1",
                name = "Apollo Pharmacy",
                address = "Shop 12, Main Road, $loc",
                distanceKm = 0.6,
                isOpen = true,
                workingHours = "24 Hours Open",
                phoneNumber = "+91 98765 43210",
                deliveryAvailable = true,
                rating = 4.8,
                locality = loc,
                medicines = listOf(
                    PharmacyMedicineItemDto("m1", "Paracetamol 650mg", "Paracetamol", 110.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = true),
                    PharmacyMedicineItemDto("m2", "Cetirizine 10mg", "Cetirizine", 80.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = true),
                    PharmacyMedicineItemDto("m3", "Pantoprazole 40mg", "Pantoprazole", 95.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = true),
                    PharmacyMedicineItemDto("m4", "Amoxicillin 500mg", "Amoxicillin", 145.0, isInStock = true, requiresPrescription = true, dosageForm = "Capsule", deliveryAvailable = true),
                    PharmacyMedicineItemDto("m5", "Azithromycin 500mg", "Azithromycin", 130.0, isInStock = true, requiresPrescription = true, dosageForm = "Tablet", deliveryAvailable = true)
                ),
                hasInventoryApiData = true
            ),
            PharmacyDetailsDto(
                id = "pharmacy_medplus_2",
                name = "MedPlus",
                address = "Plot 45, Near Metro Pillar, $loc",
                distanceKm = 0.9,
                isOpen = true,
                workingHours = "7:00 AM - 11:00 PM",
                phoneNumber = "+91 98765 12345",
                deliveryAvailable = true,
                rating = 4.6,
                locality = loc,
                medicines = listOf(
                    PharmacyMedicineItemDto("m1", "Paracetamol 650mg", "Paracetamol", 95.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = true),
                    PharmacyMedicineItemDto("m2", "Cetirizine 10mg", "Cetirizine", 75.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = true),
                    PharmacyMedicineItemDto("m3", "Pantoprazole 40mg", "Pantoprazole", 90.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = true),
                    PharmacyMedicineItemDto("m4", "Amoxicillin 500mg", "Amoxicillin", 140.0, isInStock = true, requiresPrescription = true, dosageForm = "Capsule", deliveryAvailable = true)
                ),
                hasInventoryApiData = true
            ),
            PharmacyDetailsDto(
                id = "pharmacy_sri_sai_3",
                name = "Sri Sai Medicals",
                address = "Cross Road 3, $loc",
                distanceKm = 1.2,
                isOpen = false,
                workingHours = "8:30 AM - 9:30 PM",
                phoneNumber = "+91 94401 23456",
                deliveryAvailable = false,
                rating = 4.3,
                locality = loc,
                medicines = listOf(
                    PharmacyMedicineItemDto("m1", "Paracetamol 650mg", "Paracetamol", 120.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = false),
                    PharmacyMedicineItemDto("m2", "Cetirizine 10mg", "Cetirizine", 85.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = false),
                    PharmacyMedicineItemDto("m4", "Amoxicillin 500mg", "Amoxicillin", 150.0, isInStock = true, requiresPrescription = true, dosageForm = "Capsule", deliveryAvailable = false)
                ),
                hasInventoryApiData = true
            ),
            PharmacyDetailsDto(
                id = "pharmacy_wellness_4",
                name = "Wellness Forever",
                address = "Sector 2, Highway Road, $loc",
                distanceKm = 1.8,
                isOpen = true,
                workingHours = "24 Hours Open",
                phoneNumber = "+91 99887 66554",
                deliveryAvailable = true,
                rating = 4.7,
                locality = loc,
                medicines = listOf(
                    PharmacyMedicineItemDto("m1", "Paracetamol 650mg", "Paracetamol", 105.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = true),
                    PharmacyMedicineItemDto("m3", "Pantoprazole 40mg", "Pantoprazole", 92.0, isInStock = true, requiresPrescription = false, dosageForm = "Tablet", deliveryAvailable = true)
                ),
                hasInventoryApiData = true
            ),
            PharmacyDetailsDto(
                id = "pharmacy_local_5",
                name = "Shiva Health Care & Pharmacy",
                address = "Bazaar Street, $loc",
                distanceKm = 2.4,
                isOpen = true,
                workingHours = "9:00 AM - 10:00 PM",
                phoneNumber = "+91 91234 56789",
                deliveryAvailable = false,
                rating = 4.1,
                locality = loc,
                medicines = emptyList(),
                hasInventoryApiData = false // demonstrates missing inventory gracefully
            )
        )
        _nearbyPharmaciesState.value = ApiResult.Success(demoPharmacies.sortedBy { it.distanceKm })
    }

    fun selectPharmacy(pharmacy: PharmacyDetailsDto?) {
        _selectedPharmacy.value = pharmacy
    }

    fun setSortOption(option: PharmacySortOption) {
        _sortOption.value = option
    }

    fun searchMedicines(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _medicineCatalogState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<MedicineApi>(context, ApiConfig.ENDPOINT_MEDICINE)
            if (api == null) {
                // Filter demo medicines from nearby pharmacies if search query is provided
                val queryLower = query.trim().lowercase()
                val currentPharmacies = (_nearbyPharmaciesState.value as? ApiResult.Success)?.data ?: emptyList()
                val allMedNames = currentPharmacies.flatMap { it.medicines }
                    .filter { if (queryLower.isBlank()) true else it.name.lowercase().contains(queryLower) || it.genericName.lowercase().contains(queryLower) }
                    .distinctBy { it.name }

                val demoCatalog = allMedNames.map { med ->
                    MedicineCatalogDto(
                        id = med.id,
                        name = med.name,
                        genericName = med.genericName,
                        manufacturer = "Standard Pharma Ltd.",
                        strength = "Standard Dosage",
                        form = med.dosageForm,
                        generalPurpose = "General wellness & symptomatic relief",
                        precautions = "Consult a licensed doctor/pharmacist before dosage.",
                        storageInfo = "Store below 30°C in a dry place.",
                        requiresPrescription = med.requiresPrescription
                    )
                }
                _medicineCatalogState.value = ApiResult.Success(demoCatalog)
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
                // Build price comparison from nearby pharmacies
                val currentPharmacies = (_nearbyPharmaciesState.value as? ApiResult.Success)?.data ?: emptyList()
                val matchedOffers = currentPharmacies.mapNotNull { ph ->
                    val matchingMed = ph.medicines.firstOrNull { it.name.equals(medicineName, ignoreCase = true) || it.genericName.equals(medicineName, ignoreCase = true) }
                    if (matchingMed != null && matchingMed.price != null) {
                        PharmacyOfferDto(
                            pharmacyId = ph.id,
                            pharmacyName = ph.name,
                            distanceKm = ph.distanceKm,
                            isInStock = matchingMed.isInStock,
                            price = matchingMed.price,
                            estimatedDeliveryMinutes = (ph.distanceKm * 15).toInt().coerceAtLeast(20),
                            isVerified = true,
                            contactPhone = ph.phoneNumber ?: "+91 90000 00000",
                            deliveryAvailable = ph.deliveryAvailable && matchingMed.deliveryAvailable,
                            locality = ph.locality
                        )
                    } else null
                }
                _pharmacyOffersState.value = ApiResult.Success(matchedOffers)
                return@launch
            }

            val lat = _userLocation.value?.latitude ?: 17.3850
            val lng = _userLocation.value?.longitude ?: 78.4867
            val result = RetrofitClientProvider.safeApiCall("Pharmacy Comparison") {
                api.comparePharmaciesForMedicine(medicineName = medicineName, lat = lat, lng = lng)
            }
            _pharmacyOffersState.value = result
        }
    }

    fun selectMedicine(medicine: MedicineCatalogDto) {
        _selectedMedicine.value = medicine
        comparePharmacies(medicine.name)
    }

    fun clearSelectedMedicine() {
        _selectedMedicine.value = null
    }
}

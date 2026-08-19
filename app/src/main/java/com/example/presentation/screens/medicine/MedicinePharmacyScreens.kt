package com.example.presentation.screens.medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.ApiResult
import com.example.data.remote.dto.MedicineCatalogDto
import com.example.data.remote.dto.PharmacyOfferDto
import com.example.presentation.common.ApiUnavailableCard
import com.example.presentation.viewmodel.MedicinePharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinePharmacyScreen(
    viewModel: MedicinePharmacyViewModel,
    onNavigateToApiConfig: () -> Unit
) {
    var queryText by remember { mutableStateOf("") }
    val catalogState by viewModel.medicineCatalogState.collectAsState()
    val offersState by viewModel.pharmacyOffersState.collectAsState()
    val selectedMedicine by viewModel.selectedMedicine.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine & Pharmacy Search", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = queryText,
                onValueChange = {
                    queryText = it
                    if (it.length >= 2) viewModel.searchMedicines(it)
                },
                placeholder = { Text("Search medicine name or generic composition...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (queryText.isNotEmpty()) {
                        IconButton(onClick = {
                            queryText = ""
                            viewModel.searchMedicines("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("input_medicine_search")
            )

            // Results Section
            if (selectedMedicine == null) {
                when (val state = catalogState) {
                    is ApiResult.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ApiResult.Unconfigured -> {
                        ApiUnavailableCard(
                            serviceName = state.serviceName,
                            endpointName = state.requiredEndpoint,
                            onConfigureClick = onNavigateToApiConfig
                        )
                    }
                    is ApiResult.Success -> {
                        val medicines = state.data
                        if (medicines.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No medicines found in catalog.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(medicines) { med ->
                                    MedicineCatalogCard(
                                        medicine = med,
                                        onComparePharmacies = { viewModel.selectMedicine(med) }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            } else {
                // Price Comparison View
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(selectedMedicine!!.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Generic: ${selectedMedicine!!.genericName}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.searchMedicines(queryText) }) {
                            Icon(Icons.Default.Close, contentDescription = "Back")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("VERIFIED PHARMACY PRICE COMPARISON", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    when (val oState = offersState) {
                        is ApiResult.Loading -> {
                            CircularProgressIndicator()
                        }
                        is ApiResult.Unconfigured -> {
                            ApiUnavailableCard(
                                serviceName = oState.serviceName,
                                endpointName = oState.requiredEndpoint,
                                onConfigureClick = onNavigateToApiConfig
                            )
                        }
                        is ApiResult.Success -> {
                            val offers = oState.data
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(offers) { offer ->
                                    PharmacyComparisonCard(offer = offer)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineCatalogCard(medicine: MedicineCatalogDto, onComparePharmacies: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(medicine.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(medicine.form, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
            Text("Generic: ${medicine.genericName} • Mfg: ${medicine.manufacturer}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Purpose: ${medicine.generalPurpose}", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onComparePharmacies,
                modifier = Modifier.fillMaxWidth().testTag("btn_compare_pharmacies")
            ) {
                Text("Compare Pharmacy Prices & Delivery")
            }
        }
    }
}

@Composable
fun PharmacyComparisonCard(offer: PharmacyOfferDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(offer.pharmacyName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("${offer.distanceKm} km away • Est. ${offer.estimatedDeliveryMinutes} mins", style = MaterialTheme.typography.bodySmall)
                Text(if (offer.isInStock) "In Stock" else "Out of Stock", color = if (offer.isInStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${offer.price.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text("Verified", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

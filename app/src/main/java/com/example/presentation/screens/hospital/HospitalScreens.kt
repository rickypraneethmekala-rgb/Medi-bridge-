package com.example.presentation.screens.hospital

import androidx.compose.foundation.clickable
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
import com.example.data.remote.dto.DoctorDto
import com.example.data.remote.dto.HospitalDto
import com.example.presentation.common.ApiUnavailableCard
import com.example.presentation.viewmodel.HospitalDoctorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDiscoveryScreen(
    viewModel: HospitalDoctorViewModel,
    onDoctorSelected: (DoctorDto, HospitalDto) -> Unit,
    onNavigateToApiConfig: () -> Unit
) {
    val hospitalsState by viewModel.hospitalsState.collectAsState()
    val doctorsState by viewModel.doctorsState.collectAsState()
    val selectedHospital by viewModel.selectedHospital.collectAsState()
    val currentFilter by viewModel.selectedTypeFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedHospital == null) "Hospital Discovery" else selectedHospital!!.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (selectedHospital != null) {
                        IconButton(onClick = { viewModel.loadHospitals() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadHospitals() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedHospital == null) {
                // Filter Tabs (All, Government, Private)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SegmentedButton(
                        selected = currentFilter == "ALL",
                        onClick = { viewModel.setFilter("ALL") },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("All")
                    }
                    SegmentedButton(
                        selected = currentFilter == "GOVERNMENT",
                        onClick = { viewModel.setFilter("GOVERNMENT") },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("Government")
                    }
                    SegmentedButton(
                        selected = currentFilter == "PRIVATE",
                        onClick = { viewModel.setFilter("PRIVATE") },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("Private")
                    }
                }

                when (val state = hospitalsState) {
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
                    is ApiResult.HttpError -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(state.userMessage, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is ApiResult.NetworkError -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is ApiResult.Success -> {
                        val hospitals = state.data
                        if (hospitals.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hospitals found for selected filter.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(hospitals) { hospital ->
                                    HospitalListItemCard(
                                        hospital = hospital,
                                        onClick = { viewModel.selectHospital(hospital) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Doctors List for Selected Hospital
                when (val dState = doctorsState) {
                    is ApiResult.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ApiResult.Unconfigured -> {
                        ApiUnavailableCard(
                            serviceName = dState.serviceName,
                            endpointName = dState.requiredEndpoint,
                            onConfigureClick = onNavigateToApiConfig
                        )
                    }
                    is ApiResult.Success -> {
                        val doctors = dState.data
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(selectedHospital!!.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(selectedHospital!!.address, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Departments: ${selectedHospital!!.departments.joinToString(", ")}", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            items(doctors) { doctor ->
                                DoctorListItemCard(
                                    doctor = doctor,
                                    onSelect = { onDoctorSelected(doctor, selectedHospital!!) }
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun HospitalListItemCard(hospital: HospitalDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("hospital_card_${hospital.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(hospital.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    color = if (hospital.type == "GOVERNMENT") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = hospital.type,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(hospital.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(hospital.phone, style = MaterialTheme.typography.labelSmall)
                }
                Text("View Doctors →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DoctorListItemCard(doctor: DoctorDto, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doctor_card_${doctor.id}"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(doctor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${doctor.specialization} • ${doctor.department}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Text("₹${doctor.consultationFee.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Current Queue Token: #${doctor.currentQueueToken}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text("Est. Wait: ~${doctor.estimatedWaitMinutes} mins", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onSelect,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_book_doctor_${doctor.id}")
                ) {
                    Text("Book Slot", fontSize = 13.sp)
                }
            }
        }
    }
}

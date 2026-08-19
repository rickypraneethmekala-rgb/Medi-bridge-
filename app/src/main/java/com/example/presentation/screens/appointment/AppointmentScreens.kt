package com.example.presentation.screens.appointment

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
import com.example.data.remote.dto.AppointmentDto
import com.example.presentation.common.ApiUnavailableCard
import com.example.presentation.viewmodel.AppointmentQueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentQueueScreen(
    viewModel: AppointmentQueueViewModel,
    onNavigateToApiConfig: () -> Unit
) {
    val appointmentsState by viewModel.appointmentsState.collectAsState()
    val liveQueueState by viewModel.liveQueueState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments & Live Queue", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadAppointments() }) {
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My Bookings") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Live Queue Tracker") }
                )
            }

            if (selectedTab == 0) {
                when (val state = appointmentsState) {
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
                        val list = state.data
                        if (list.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No appointments scheduled. Book via Hospital Discovery.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(list) { appointment ->
                                    AppointmentItemCard(
                                        appointment = appointment,
                                        onTrackQueue = {
                                            selectedTab = 1
                                            viewModel.refreshLiveQueue(appointment.doctorName)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            } else {
                // Live Queue Tracker View
                when (val qState = liveQueueState) {
                    is ApiResult.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ApiResult.Unconfigured -> {
                        ApiUnavailableCard(
                            serviceName = qState.serviceName,
                            endpointName = qState.requiredEndpoint,
                            onConfigureClick = onNavigateToApiConfig
                        )
                    }
                    is ApiResult.Success -> {
                        val queue = qState.data
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("LIVE CONSULTATION QUEUE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(queue.doctorName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Serving Now", style = MaterialTheme.typography.labelMedium)
                                            Text("#${queue.currentServingToken}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Your Token", style = MaterialTheme.typography.labelMedium)
                                            Text("#${queue.patientToken}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Patients Ahead: ${queue.patientsAhead}", fontWeight = FontWeight.SemiBold)
                                        Text("Est. Delay: ~${queue.estimatedWaitMinutes} mins", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
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
fun AppointmentItemCard(appointment: AppointmentDto, onTrackQueue: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("appointment_card_${appointment.appointmentId}"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(appointment.doctorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${appointment.department} • ${appointment.hospitalName}", style = MaterialTheme.typography.bodySmall)
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Token #${appointment.tokenNumber}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Slot: ${appointment.slotDateTime}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onTrackQueue, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("Track Queue", fontSize = 12.sp)
                }
            }
        }
    }
}

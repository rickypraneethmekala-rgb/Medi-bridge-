package com.example.presentation.screens.order

import androidx.compose.foundation.layout.*
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
import com.example.presentation.common.ApiUnavailableCard
import com.example.presentation.viewmodel.OrderDeliveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDeliveryScreen(
    viewModel: OrderDeliveryViewModel,
    onNavigateToApiConfig: () -> Unit
) {
    val trackingState by viewModel.trackingState.collectAsState()
    val otpState by viewModel.otpVerificationState.collectAsState()
    var inputOtp by remember { mutableStateOf("") }
    var orderIdQuery by remember { mutableStateOf("ORD_1001") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order & Delivery Tracking", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = orderIdQuery,
                onValueChange = { orderIdQuery = it },
                label = { Text("Order ID") },
                trailingIcon = {
                    IconButton(onClick = { viewModel.trackOrder(orderIdQuery) }) {
                        Icon(Icons.Default.Search, contentDescription = "Track")
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("input_order_track_id")
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = trackingState) {
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
                    val order = state.data
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("ORDER STATUS: ${order.status}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Pharmacy: ${order.pharmacyName}", style = MaterialTheme.typography.bodySmall)
                            Text("Total: ₹${order.totalAmount.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Delivery Partner: ${order.deliveryPartnerName ?: "Assigning Partner..."}", fontWeight = FontWeight.SemiBold)
                            if (order.estimatedArrival != null) {
                                Text("ETA: ${order.estimatedArrival}", style = MaterialTheme.typography.labelSmall)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("DELIVERY CONFIRMATION OTP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(order.deliveryOtp, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                                    Text("Share this with the delivery partner upon arrival", style = MaterialTheme.typography.labelSmall)
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

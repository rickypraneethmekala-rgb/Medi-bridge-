package com.example.presentation.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.ApiResult
import com.example.data.remote.dto.PrescriptionOrder
import com.example.data.remote.dto.PrescriptionOrderStatus
import com.example.presentation.common.ApiUnavailableCard
import com.example.presentation.screens.medicine.StatusBadge
import com.example.presentation.viewmodel.OrderDeliveryViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDeliveryScreen(
    viewModel: OrderDeliveryViewModel,
    onNavigateToApiConfig: () -> Unit
) {
    val prescriptionOrders by viewModel.prescriptionOrders.collectAsState()
    var selectedOrder by remember { mutableStateOf<PrescriptionOrder?>(null) }

    LaunchedEffect(prescriptionOrders) {
        if (selectedOrder == null && prescriptionOrders.isNotEmpty()) {
            selectedOrder = prescriptionOrders.first()
        } else if (selectedOrder != null) {
            selectedOrder = prescriptionOrders.find { it.id == selectedOrder!!.id } ?: prescriptionOrders.firstOrNull()
        }
    }

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
            if (prescriptionOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No Active Prescription Orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Upload a prescription in the Pharmacy screen to track orders and delivery.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Text("YOUR MEDICINE ORDERS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal list or dropdown of orders
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    prescriptionOrders.take(3).forEach { ord ->
                        val isSelected = selectedOrder?.id == ord.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedOrder = ord },
                            label = { Text("#${ord.id} (${ord.pharmacyName.take(8)})", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                selectedOrder?.let { order ->
                    TrackingOrderCard(order = order)
                }
            }
        }
    }
}

@Composable
fun TrackingOrderCard(order: PrescriptionOrder) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(order.createdAt) { dateFormat.format(Date(order.createdAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order #${order.id}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Pharmacy: ${order.pharmacyName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Placed on: $formattedDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Indicator
            OrderWorkflowProgress(status = order.status)

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery OTP Card (if preparing or out for delivery)
            if (order.status == PrescriptionOrderStatus.OUT_FOR_DELIVERY || order.status == PrescriptionOrderStatus.PREPARING || order.status == PrescriptionOrderStatus.PAYMENT_COMPLETED) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("DELIVERY CONFIRMATION OTP", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(order.deliveryOtp, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Share this 4-digit OTP only with your MediBridge delivery executive", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (order.deliveryPartnerName != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DirectionsBike, contentDescription = null, tint = MediTeal)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Delivery Partner", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(order.deliveryPartnerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Estimated arrival in ~${order.estimatedDeliveryMinutes} mins", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Summary of Verified Medicines
            if (order.verifiedMedicines.isNotEmpty()) {
                Text("ORDER SUMMARY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                order.verifiedMedicines.forEach { med ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${med.quantity}x ${med.medicineName} (${med.strength})", style = MaterialTheme.typography.bodySmall)
                        Text("₹${(med.price * med.quantity).toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Divider()
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Delivery Fee", style = MaterialTheme.typography.bodySmall)
                    Text("₹${order.deliveryFee.toInt()}", style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Paid", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun OrderWorkflowProgress(status: PrescriptionOrderStatus) {
    val steps = listOf(
        "Upload" to (status.ordinal >= PrescriptionOrderStatus.PRESCRIPTION_RECEIVED.ordinal),
        "Pharmacist Review" to (status.ordinal >= PrescriptionOrderStatus.PHARMACIST_REVIEWING.ordinal && status != PrescriptionOrderStatus.REJECTED),
        "Verified" to (status.ordinal >= PrescriptionOrderStatus.PRESCRIPTION_VERIFIED.ordinal && status != PrescriptionOrderStatus.REJECTED),
        "Paid" to (status.ordinal >= PrescriptionOrderStatus.PAYMENT_COMPLETED.ordinal && status != PrescriptionOrderStatus.REJECTED),
        "Packed" to (status.ordinal >= PrescriptionOrderStatus.PREPARING.ordinal && status != PrescriptionOrderStatus.REJECTED),
        "Delivered" to (status == PrescriptionOrderStatus.DELIVERED)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("ORDER LIFECYCLE PROGRESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        steps.forEachIndexed { index, (name, isCompleted) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .width(2.dp)
                        .height(10.dp)
                        .background(if (steps[index + 1].second) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                )
            }
        }
    }
}

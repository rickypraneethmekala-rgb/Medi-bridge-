package com.example.presentation.screens.portals

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.security.AuthUser
import com.example.core.security.UserRole
import com.example.core.storage.DocumentStorageHelper
import com.example.data.remote.dto.PrescriptionOrder
import com.example.data.remote.dto.PrescriptionOrderStatus
import com.example.data.remote.dto.VerifiedMedicineItem
import com.example.presentation.common.ApiUnavailableCard
import com.example.presentation.common.MediOutlinedTextField
import com.example.ui.theme.*
import com.example.presentation.screens.medicine.StatusBadge
import com.example.presentation.viewmodel.OrderDeliveryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolePortalScreen(
    user: AuthUser,
    orderViewModel: OrderDeliveryViewModel = viewModel(),
    onNavigateToApiConfig: () -> Unit,
    onSwitchRole: (UserRole) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${user.role.displayName} Portal", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Verified Officer: ${user.fullName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Role: ${user.role.displayName} (${user.role.name})", style = MaterialTheme.typography.bodySmall)
                    Text("Session ID: ${user.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (user.role) {
                    UserRole.DOCTOR -> {
                        Column {
                            Text("TODAY'S CLINICAL QUEUE & DIGITAL PRESCRIPTIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            ApiUnavailableCard(
                                serviceName = "Doctor Queue Manager",
                                endpointName = "DOCTOR_API_URL",
                                onConfigureClick = onNavigateToApiConfig
                            )
                        }
                    }
                    UserRole.PHARMACIST -> {
                        PharmacistPortalView(orderViewModel = orderViewModel)
                    }
                    UserRole.DELIVERY_PARTNER -> {
                        DeliveryPartnerPortalView(orderViewModel = orderViewModel)
                    }
                    UserRole.HOSPITAL_ADMIN, UserRole.SUPER_ADMIN -> {
                        Column {
                            Text("FACILITY ROSTER, DEPARTMENTS & AUDIT LOGS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            ApiUnavailableCard(
                                serviceName = "Hospital Admin & Audit Gateway",
                                endpointName = "BASE_URL",
                                onConfigureClick = onNavigateToApiConfig
                            )
                        }
                    }
                    UserRole.PATIENT -> {}
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Demo Role Switcher to easily test different interfaces
            Text("SWITCH DEMO ROLE VIEW:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                UserRole.entries.take(3).forEach { r ->
                    OutlinedButton(
                        onClick = { onSwitchRole(r) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(r.displayName, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                UserRole.entries.drop(3).forEach { r ->
                    OutlinedButton(
                        onClick = { onSwitchRole(r) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(r.displayName, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Pharmacist Portal View
// -------------------------------------------------------------
@Composable
fun PharmacistPortalView(orderViewModel: OrderDeliveryViewModel) {
    val orders by orderViewModel.prescriptionOrders.collectAsState()
    var selectedOrderForVerification by remember { mutableStateOf<PrescriptionOrder?>(null) }
    var selectedOrderForClarification by remember { mutableStateOf<PrescriptionOrder?>(null) }
    var selectedOrderForRejection by remember { mutableStateOf<PrescriptionOrder?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PRESCRIPTION REQUESTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("${orders.size} Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pending prescription requests at this time.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    PharmacistOrderCard(
                        order = order,
                        onStartReview = { orderViewModel.pharmacistStartReview(order.id) },
                        onVerifyClick = { selectedOrderForVerification = order },
                        onRequestClarificationClick = { selectedOrderForClarification = order },
                        onRejectClick = { selectedOrderForRejection = order },
                        onPrepareOrder = { orderViewModel.pharmacyPrepareOrder(order.id) },
                        onDispatchToDelivery = { orderViewModel.deliveryDispatch(order.id) }
                    )
                }
            }
        }
    }

    // Verification Dialog
    if (selectedOrderForVerification != null) {
        PharmacistVerificationDialog(
            order = selectedOrderForVerification!!,
            onDismiss = { selectedOrderForVerification = null },
            onConfirm = { medicines ->
                orderViewModel.pharmacistVerifyPrescription(selectedOrderForVerification!!.id, medicines)
                selectedOrderForVerification = null
            }
        )
    }

    // Clarification Request Dialog
    if (selectedOrderForClarification != null) {
        PharmacistClarificationRequestDialog(
            order = selectedOrderForClarification!!,
            onDismiss = { selectedOrderForClarification = null },
            onSubmit = { message, requiresNewImage ->
                orderViewModel.pharmacistRequestClarification(selectedOrderForClarification!!.id, message, requiresNewImage)
                selectedOrderForClarification = null
            }
        )
    }

    // Rejection Dialog
    if (selectedOrderForRejection != null) {
        PharmacistRejectionDialog(
            order = selectedOrderForRejection!!,
            onDismiss = { selectedOrderForRejection = null },
            onSubmit = { reason ->
                orderViewModel.pharmacistRejectPrescription(selectedOrderForRejection!!.id, reason)
                selectedOrderForRejection = null
            }
        )
    }
}

@Composable
fun PharmacistOrderCard(
    order: PrescriptionOrder,
    onStartReview: () -> Unit,
    onVerifyClick: () -> Unit,
    onRequestClarificationClick: () -> Unit,
    onRejectClick: () -> Unit,
    onPrepareOrder: () -> Unit,
    onDispatchToDelivery: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(order.createdAt) { dateFormat.format(Date(order.createdAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pharmacist_order_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Req #${order.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Patient: ${order.patientName} (${order.patientPhone})", style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Date: $formattedDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (order.patientNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Patient Note: \"${order.patientNote}\"", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            // Prescription image or PDF indicator
            if (order.prescriptionFilePath.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                if (order.prescriptionFileType == "IMAGE") {
                    val bitmap = remember(order.prescriptionFilePath) {
                        try {
                            BitmapFactory.decodeFile(order.prescriptionFilePath)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Prescription image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { DocumentStorageHelper.openPdfWithViewer(context, order.prescriptionFilePath) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Prescription PDF", fontSize = 11.sp)
                    }
                }
            }

            // Clarification history
            if (order.clarifications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Clarification thread (${order.clarifications.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                order.clarifications.takeLast(2).forEach { msg ->
                    Text("• ${msg.senderName}: ${msg.message}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            when (order.status) {
                PrescriptionOrderStatus.PRESCRIPTION_RECEIVED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onStartReview, modifier = Modifier.weight(1f)) {
                            Text("Start Review", fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = onRejectClick, modifier = Modifier.weight(1f)) {
                            Text("Reject", fontSize = 12.sp)
                        }
                    }
                }

                PrescriptionOrderStatus.PHARMACIST_REVIEWING,
                PrescriptionOrderStatus.CLARIFICATION_RECEIVED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onVerifyClick,
                            modifier = Modifier.weight(1.2f).testTag("btn_pharmacist_verify_${order.id}"),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verify", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = onRequestClarificationClick,
                            modifier = Modifier.weight(1.3f).testTag("btn_pharmacist_clarify_${order.id}"),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clarify", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = onRejectClick,
                            modifier = Modifier.weight(1f).testTag("btn_pharmacist_reject_${order.id}"),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Reject", fontSize = 11.sp)
                        }
                    }
                }

                PrescriptionOrderStatus.PAYMENT_COMPLETED -> {
                    Button(onClick = onPrepareOrder, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Inventory, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Prepare Medicines & Pack")
                    }
                }

                PrescriptionOrderStatus.PREPARING -> {
                    Button(onClick = onDispatchToDelivery, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch to Delivery Partner")
                    }
                }

                else -> {}
            }
        }
    }
}

// -------------------------------------------------------------
// Pharmacist Verification Dialog
// -------------------------------------------------------------
@Composable
fun PharmacistVerificationDialog(
    order: PrescriptionOrder,
    onDismiss: () -> Unit,
    onConfirm: (List<VerifiedMedicineItem>) -> Unit
) {
    val defaultMedicines = remember {
        mutableStateListOf(
            VerifiedMedicineItem(id = UUID.randomUUID().toString(), medicineName = "Paracetamol 500mg", strength = "500mg", quantity = 10, form = "Tablet", price = 11.0, isInStock = true),
            VerifiedMedicineItem(id = UUID.randomUUID().toString(), medicineName = "Cetirizine 10mg", strength = "10mg", quantity = 5, form = "Tablet", price = 16.0, isInStock = true)
        )
    }

    var newMedName by remember { mutableStateOf("") }
    var newMedQty by remember { mutableStateOf("10") }
    var newMedPrice by remember { mutableStateOf("100") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Verify Prescription", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Confirm medicines, quantity, pricing, and stock availability as per doctor's prescription.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                Text("CONFIRMED MEDICINES:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                defaultMedicines.forEachIndexed { index, med ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(med.medicineName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Qty: ${med.quantity} • Form: ${med.form} • ₹${(med.price * med.quantity).toInt()}", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { defaultMedicines.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Custom Medicine Input
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MediOutlinedTextField(
                        value = newMedName,
                        onValueChange = { newMedName = it },
                        label = { Text("Medicine") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    MediOutlinedTextField(
                        value = newMedQty,
                        onValueChange = { newMedQty = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    MediOutlinedTextField(
                        value = newMedPrice,
                        onValueChange = { newMedPrice = it },
                        label = { Text("Price (₹)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        if (newMedName.isNotBlank()) {
                            val qty = newMedQty.toIntOrNull() ?: 1
                            val pr = (newMedPrice.toDoubleOrNull() ?: 50.0) / qty
                            defaultMedicines.add(
                                VerifiedMedicineItem(
                                    id = UUID.randomUUID().toString(),
                                    medicineName = newMedName,
                                    quantity = qty,
                                    price = pr,
                                    isInStock = true
                                )
                            )
                            newMedName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Another Medicine", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(defaultMedicines.toList()) },
                        modifier = Modifier.weight(1f),
                        enabled = defaultMedicines.isNotEmpty()
                    ) {
                        Text("Verify & Send")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Pharmacist Clarification Request Dialog
// -------------------------------------------------------------
@Composable
fun PharmacistClarificationRequestDialog(
    order: PrescriptionOrder,
    onDismiss: () -> Unit,
    onSubmit: (message: String, requiresNewImage: Boolean) -> Unit
) {
    var reasonText by remember { mutableStateOf("Medicine name on line 2 is unclear. Please upload a clearer image.") }
    var requestNewImage by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Request Clarification", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Please explain what needs clarification from the patient.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                MediOutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Clarification Message") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requestNewImage = !requestNewImage },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = requestNewImage, onCheckedChange = { requestNewImage = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request clearer prescription photo / re-upload", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSubmit(reasonText, requestNewImage) },
                        modifier = Modifier.weight(1f),
                        enabled = reasonText.isNotBlank()
                    ) {
                        Text("Send Request")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Pharmacist Rejection Dialog
// -------------------------------------------------------------
@Composable
fun PharmacistRejectionDialog(
    order: PrescriptionOrder,
    onDismiss: () -> Unit,
    onSubmit: (reason: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Prescription image is unreadable.") }

    val presetReasons = listOf(
        "Prescription image is unreadable.",
        "Prescription is expired.",
        "Prescription information is incomplete.",
        "Prescription cannot be fulfilled at this pharmacy."
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Reject Prescription", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(4.dp))
                Text("A rejection reason is required so the patient understands why it could not be fulfilled.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                presetReasons.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedReason == r, onClick = { selectedReason = r })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(r, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSubmit(selectedReason) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm Reject")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Delivery Partner Portal View
// -------------------------------------------------------------
@Composable
fun DeliveryPartnerPortalView(orderViewModel: OrderDeliveryViewModel) {
    val context = LocalContext.current
    val orders by orderViewModel.prescriptionOrders.collectAsState()
    val activeDeliveries = orders.filter { it.status == PrescriptionOrderStatus.OUT_FOR_DELIVERY || it.status == PrescriptionOrderStatus.PREPARING }

    var enteredOtpMap by remember { mutableStateOf(mapOf<String, String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("ACTIVE DELIVERY DISPATCHES & OTP VERIFICATION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        if (activeDeliveries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active delivery dispatches assigned.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeDeliveries, key = { it.id }) { order ->
                    val enteredOtp = enteredOtpMap[order.id] ?: ""

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Order #${order.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                StatusBadge(status = order.status)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Customer: ${order.patientName} (${order.patientPhone})", style = MaterialTheme.typography.bodySmall)
                            Text("Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Pharmacy: ${order.pharmacyName}", style = MaterialTheme.typography.bodySmall)
                            Text("Amount: ₹${order.totalAmount.toInt()} (Paid Online)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

                            Spacer(modifier = Modifier.height(10.dp))

                            if (order.status == PrescriptionOrderStatus.OUT_FOR_DELIVERY) {
                                Text("ENTER 4-DIGIT CUSTOMER OTP:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MediOutlinedTextField(
                                        value = enteredOtp,
                                        onValueChange = { enteredOtpMap = enteredOtpMap + (order.id to it) },
                                        placeholder = { Text("e.g. 5821") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = {
                                            val success = orderViewModel.verifyDeliveryOtpLocal(order.id, enteredOtp)
                                            if (success) {
                                                Toast.makeText(context, "OTP verified! Order delivered.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Invalid OTP. Please verify with patient.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    ) {
                                        Text("Complete")
                                    }
                                }
                            } else {
                                Text("Waiting for pharmacy to finish packing...", style = MaterialTheme.typography.labelSmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                }
            }
        }
    }
}

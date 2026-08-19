package com.example.presentation.screens.medicine

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.maps.GoogleMapsLauncher
import com.example.core.network.ApiResult
import com.example.core.storage.DocumentStorageHelper
import com.example.data.remote.dto.MedicineCatalogDto
import com.example.data.remote.dto.PharmacyDetailsDto
import com.example.data.remote.dto.PharmacyMedicineItemDto
import com.example.data.remote.dto.PharmacyOfferDto
import com.example.data.remote.dto.PrescriptionOrder
import com.example.data.remote.dto.PrescriptionOrderStatus
import com.example.presentation.viewmodel.MedicinePharmacyViewModel
import com.example.presentation.viewmodel.OrderDeliveryViewModel
import com.example.presentation.viewmodel.PharmacySortOption
import com.example.presentation.common.MediOutlinedTextField
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinePharmacyScreen(
    viewModel: MedicinePharmacyViewModel,
    orderViewModel: OrderDeliveryViewModel,
    onNavigateToApiConfig: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val locationPermissionGranted by viewModel.locationPermissionGranted.collectAsState()
    val isGpsEnabled by viewModel.isGpsEnabled.collectAsState()
    val nearbyPharmaciesState by viewModel.nearbyPharmaciesState.collectAsState()
    val selectedPharmacy by viewModel.selectedPharmacy.collectAsState()
    val selectedMedicine by viewModel.selectedMedicine.collectAsState()
    val pharmacyOffersState by viewModel.pharmacyOffersState.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val prescriptionOrders by orderViewModel.prescriptionOrders.collectAsState()

    var activeOrderOffer by remember { mutableStateOf<PharmacyOfferDto?>(null) }
    var showOrderSuccess by remember { mutableStateOf(false) }
    var showUploadPrescriptionDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Nearby & Search, 1: Prescription Orders
    var orderForClarification by remember { mutableStateOf<PrescriptionOrder?>(null) }
    var orderForPayment by remember { mutableStateOf<PrescriptionOrder?>(null) }
    var viewingOrderDetails by remember { mutableStateOf<PrescriptionOrder?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(fineGranted || coarseGranted)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pharmacy & Orders", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { GoogleMapsLauncher.openNearbyMedicalShops(context, searchQuery.ifBlank { null }) },
                        modifier = Modifier.testTag("btn_top_maps_search")
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Open Nearby Medical Shops in Google Maps", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showUploadPrescriptionDialog = true },
                icon = { Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White) },
                text = { Text("Upload Prescription", fontWeight = FontWeight.Bold, color = Color.White) },
                containerColor = MediTeal,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_upload_prescription")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Location Detection & Permission Bar
            LocationDetectionBanner(
                localityName = userLocation?.locality ?: "Detected Area",
                permissionGranted = locationPermissionGranted,
                isGpsEnabled = isGpsEnabled,
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            // Top Segmented Tabs: Nearby & Catalog vs Prescription Orders
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Nearby & Search", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        val activeCount = prescriptionOrders.count { it.status != PrescriptionOrderStatus.DELIVERED && it.status != PrescriptionOrderStatus.REJECTED }
                        Text("Prescription Orders ${if (activeCount > 0) "($activeCount)" else ""}", fontWeight = FontWeight.SemiBold)
                    },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
                )
            }

            if (selectedTab == 1) {
                // Tab 2: Prescription Orders & Pharmacist Verification List
                PrescriptionOrdersTab(
                    orders = prescriptionOrders,
                    onUploadClick = { showUploadPrescriptionDialog = true },
                    onClarificationClick = { order -> orderForClarification = order },
                    onPayClick = { order -> orderForPayment = order },
                    onViewDetails = { order -> viewingOrderDetails = order }
                )
            } else {
                // Tab 1: Nearby & Search
                // Search Bar
                MediOutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.searchMedicines(it)
                    },
                    placeholder = { Text("Search medicine (e.g. Paracetamol)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchMedicines("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("input_medicine_search")
                )

                // Quick Action Bar for Uploading Prescription
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Have a doctor's prescription?",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = { showUploadPrescriptionDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MediTeal, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_quick_upload_prescription")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                // Content Switcher
                when {
                    // 1. Price Comparison View for Selected Medicine
                    selectedMedicine != null -> {
                        PriceComparisonView(
                            medicine = selectedMedicine!!,
                            offersState = pharmacyOffersState,
                            sortOption = sortOption,
                            onSortChanged = { viewModel.setSortOption(it) },
                            onBack = { viewModel.clearSelectedMedicine() },
                            onOpenMaps = { GoogleMapsLauncher.openNearbyMedicalShops(context, selectedMedicine!!.name) },
                            onOrder = { offer -> activeOrderOffer = offer }
                        )
                    }

                    // 2. Pharmacy Details View
                    selectedPharmacy != null -> {
                        PharmacyDetailsView(
                            pharmacy = selectedPharmacy!!,
                            onBack = { viewModel.selectPharmacy(null) },
                            onUploadPrescriptionForPharmacy = { showUploadPrescriptionDialog = true },
                            onSelectMedicine = { medItem ->
                                viewModel.selectMedicine(
                                    MedicineCatalogDto(
                                        id = medItem.id,
                                        name = medItem.name,
                                        genericName = medItem.genericName,
                                        manufacturer = "Standard Healthcare",
                                        strength = "Standard Dose",
                                        form = medItem.dosageForm,
                                        generalPurpose = "General Healthcare",
                                        precautions = "Take as advised by physician.",
                                        storageInfo = "Store in cool & dry place.",
                                        requiresPrescription = medItem.requiresPrescription
                                    )
                                )
                            },
                            onOpenMapsDirections = { GoogleMapsLauncher.openPharmacyDirections(context, selectedPharmacy!!.name) },
                            onCallPharmacy = { phone ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            }
                        )
                    }

                    // 3. Search Results or Nearby Medical Shops List
                    searchQuery.isNotBlank() -> {
                        MedicineSearchResultsList(
                            viewModel = viewModel,
                            searchQuery = searchQuery,
                            onSelectMedicine = { med -> viewModel.selectMedicine(med) },
                            onOpenMaps = { GoogleMapsLauncher.openNearbyMedicalShops(context, searchQuery) }
                        )
                    }

                    else -> {
                        // Default: Nearby Medical Shops
                        NearbyPharmaciesList(
                            nearbyState = nearbyPharmaciesState,
                            locality = userLocation?.locality ?: "Detected Area",
                            onSelectPharmacy = { pharmacy -> viewModel.selectPharmacy(pharmacy) },
                            onOpenMaps = { GoogleMapsLauncher.openNearbyMedicalShops(context, null) },
                            onRetry = {
                                val loc = userLocation
                                if (loc != null) viewModel.loadNearbyPharmacies(loc.latitude, loc.longitude, loc.locality)
                                else viewModel.checkLocationPermission()
                            }
                        )
                    }
                }
            }
        }
    }

    // Upload Prescription Dialog
    if (showUploadPrescriptionDialog) {
        val nearbyPharmacies = (nearbyPharmaciesState as? ApiResult.Success)?.data ?: emptyList()
        UploadPrescriptionDialog(
            nearbyPharmacies = nearbyPharmacies,
            onDismiss = { showUploadPrescriptionDialog = false },
            onSubmit = { pharmacyId, pharmacyName, filePath, fileType, patientNote ->
                orderViewModel.uploadPrescriptionRequest(
                    patientName = "Ricky",
                    patientPhone = "+91 98765 43210",
                    pharmacyId = pharmacyId,
                    pharmacyName = pharmacyName,
                    filePath = filePath,
                    fileType = fileType,
                    patientNote = patientNote
                )
                showUploadPrescriptionDialog = false
                selectedTab = 1 // switch to prescription orders tab
                Toast.makeText(context, "Prescription sent to pharmacist for verification.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Patient Clarification Response Dialog
    if (orderForClarification != null) {
        PatientClarificationDialog(
            order = orderForClarification!!,
            onDismiss = { orderForClarification = null },
            onSubmit = { replyMessage, newAttachmentPath ->
                orderViewModel.patientSubmitClarification(
                    orderId = orderForClarification!!.id,
                    replyMessage = replyMessage,
                    newAttachmentPath = newAttachmentPath
                )
                orderForClarification = null
                Toast.makeText(context, "Clarification submitted to pharmacist.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Patient Confirm & Pay Dialog
    if (orderForPayment != null) {
        PatientPaymentDialog(
            order = orderForPayment!!,
            onDismiss = { orderForPayment = null },
            onPaymentSuccess = { paymentMethod ->
                orderViewModel.patientConfirmAndPay(orderForPayment!!.id, paymentMethod)
                orderForPayment = null
                Toast.makeText(context, "Payment completed! Pharmacy is preparing your order.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // View Order Details Modal
    if (viewingOrderDetails != null) {
        val currentOrder = prescriptionOrders.find { it.id == viewingOrderDetails!!.id } ?: viewingOrderDetails!!
        PrescriptionOrderDetailsDialog(
            order = currentOrder,
            onDismiss = { viewingOrderDetails = null },
            onClarify = {
                viewingOrderDetails = null
                orderForClarification = currentOrder
            },
            onPay = {
                viewingOrderDetails = null
                orderForPayment = currentOrder
            }
        )
    }

    // Order Confirmation Dialog for catalog orders
    if (activeOrderOffer != null) {
        OrderConfirmationDialog(
            offer = activeOrderOffer!!,
            medicineName = selectedMedicine?.name ?: "Prescription Medicine",
            requiresPrescription = selectedMedicine?.requiresPrescription == true,
            onDismiss = { activeOrderOffer = null },
            onConfirm = {
                activeOrderOffer = null
                showOrderSuccess = true
            }
        )
    }

    if (showOrderSuccess) {
        AlertDialog(
            onDismissRequest = { showOrderSuccess = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Order Request Received") },
            text = {
                Text("Your medicine order request has been submitted to the pharmacy. A pharmacist will verify stock and contact you for doorstep delivery.")
            },
            confirmButton = {
                Button(onClick = { showOrderSuccess = false }) {
                    Text("Done")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// Prescription Orders Tab View
// -------------------------------------------------------------
@Composable
fun PrescriptionOrdersTab(
    orders: List<PrescriptionOrder>,
    onUploadClick: () -> Unit,
    onClarificationClick: (PrescriptionOrder) -> Unit,
    onPayClick: (PrescriptionOrder) -> Unit,
    onViewDetails: (PrescriptionOrder) -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No Prescription Orders Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Upload a photo or PDF of your doctor's prescription. A verified pharmacist will review and dispense your medicines.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onUploadClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MediTeal, contentColor = Color.White),
                    modifier = Modifier.testTag("btn_empty_upload_prescription")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Prescription", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Prescription Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onUploadClick) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Upload", fontSize = 12.sp)
                    }
                }
            }

            items(orders, key = { it.id }) { order ->
                PrescriptionOrderCard(
                    order = order,
                    onClarificationClick = { onClarificationClick(order) },
                    onPayClick = { onPayClick(order) },
                    onViewDetails = { onViewDetails(order) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun PrescriptionOrderCard(
    order: PrescriptionOrder,
    onClarificationClick: () -> Unit,
    onPayClick: () -> Unit,
    onViewDetails: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(order.createdAt) { dateFormat.format(Date(order.createdAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails)
            .testTag("card_prescription_order_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Request #${order.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.pharmacyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Submitted: $formattedDate",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (order.patientNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: \"${order.patientNote}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status-specific banner or prompt
            when (order.status) {
                PrescriptionOrderStatus.PRESCRIPTION_RECEIVED -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Waiting for pharmacist review...", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                PrescriptionOrderStatus.PHARMACIST_REVIEWING -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pharmacist is currently reviewing the prescription.", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                PrescriptionOrderStatus.CLARIFICATION_REQUIRED -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            val lastMsg = order.clarifications.lastOrNull { it.senderRole == "PHARMACIST" }?.message
                                ?: "Clarification requested regarding prescription details."
                            Text("⚠️ Pharmacist requested clarification:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(lastMsg, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onClarificationClick,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Respond / Upload New Image", fontSize = 12.sp)
                            }
                        }
                    }
                }

                PrescriptionOrderStatus.CLARIFICATION_RECEIVED -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clarification sent. Pharmacist reviewing updated details.", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                PrescriptionOrderStatus.PRESCRIPTION_VERIFIED -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("✅ Prescription Verified", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${order.verifiedMedicines.size} medicine(s) verified by pharmacist.", style = MaterialTheme.typography.bodySmall)
                            Text("Total: ₹${order.totalAmount.toInt()} (Includes ₹40 delivery fee)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onPayClick,
                                modifier = Modifier.fillMaxWidth().testTag("btn_confirm_pay_${order.id}")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Confirm & Pay ₹${order.totalAmount.toInt()}")
                            }
                        }
                    }
                }

                PrescriptionOrderStatus.PAYMENT_COMPLETED,
                PrescriptionOrderStatus.PREPARING,
                PrescriptionOrderStatus.OUT_FOR_DELIVERY -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        if (order.status == PrescriptionOrderStatus.OUT_FOR_DELIVERY) "🚚 Out for Delivery" else "📦 Preparing in Pharmacy",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    if (order.deliveryPartnerName != null) {
                                        Text("Partner: ${order.deliveryPartnerName}", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("DELIVERY OTP", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(order.deliveryOtp, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 2.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                PrescriptionOrderStatus.DELIVERED -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delivered successfully. Verified via OTP.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                PrescriptionOrderStatus.REJECTED -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Rejection Reason: ${order.rejectionReason ?: "Prescription could not be fulfilled."}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onViewDetails) {
                    Text("View Full Request", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Status Badge Helper
// -------------------------------------------------------------
@Composable
fun StatusBadge(status: PrescriptionOrderStatus) {
    val (badgeText, bgColor, textColor) = when (status) {
        PrescriptionOrderStatus.PRESCRIPTION_RECEIVED -> Triple("🟡 Waiting", Color(0xFFFFF9C4), Color(0xFF795548))
        PrescriptionOrderStatus.PHARMACIST_REVIEWING -> Triple("🔵 Reviewing", Color(0xFFE3F2FD), Color(0xFF1565C0))
        PrescriptionOrderStatus.CLARIFICATION_REQUIRED -> Triple("🟠 Clarification", Color(0xFFFFE0B2), Color(0xFFE65100))
        PrescriptionOrderStatus.CLARIFICATION_RECEIVED -> Triple("🟡 Clarification Sent", Color(0xFFFFF9C4), Color(0xFF795548))
        PrescriptionOrderStatus.PRESCRIPTION_VERIFIED -> Triple("🟢 Verified", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        PrescriptionOrderStatus.PATIENT_CONFIRMED -> Triple("🔵 Confirmed", Color(0xFFE3F2FD), Color(0xFF1565C0))
        PrescriptionOrderStatus.PAYMENT_COMPLETED -> Triple("🟢 Paid", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        PrescriptionOrderStatus.PREPARING -> Triple("📦 Preparing", Color(0xFFEDE7F6), Color(0xFF512DA8))
        PrescriptionOrderStatus.OUT_FOR_DELIVERY -> Triple("🚚 Out for Delivery", Color(0xFFEDE7F6), Color(0xFF512DA8))
        PrescriptionOrderStatus.DELIVERED -> Triple("✅ Delivered", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        PrescriptionOrderStatus.REJECTED -> Triple("🔴 Rejected", Color(0xFFFFEBEE), Color(0xFFC62828))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = badgeText,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// -------------------------------------------------------------
// Upload Prescription Dialog
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPrescriptionDialog(
    nearbyPharmacies: List<PharmacyDetailsDto>,
    onDismiss: () -> Unit,
    onSubmit: (pharmacyId: String, pharmacyName: String, filePath: String, fileType: String, patientNote: String) -> Unit
) {
    val context = LocalContext.current
    var selectedPharmacy by remember { mutableStateOf(nearbyPharmacies.firstOrNull() ?: PharmacyDetailsDto("pharmacy_apollo_1", "Apollo Pharmacy", "Main Road, Raidurg", 0.6)) }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var selectedFileType by remember { mutableStateOf("IMAGE") }
    var tempCameraPath by remember { mutableStateOf<String?>(null) }
    var patientNote by remember { mutableStateOf("Please provide all medicines mentioned in this prescription.") }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraPath != null) {
            selectedFilePath = tempCameraPath
            selectedFileType = "IMAGE"
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = DocumentStorageHelper.saveFileToPrivateStorage(context, uri, ".jpg")
            if (result != null) {
                selectedFilePath = result.second
                selectedFileType = "IMAGE"
            }
        }
    }

    // PDF launcher
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = DocumentStorageHelper.saveFileToPrivateStorage(context, uri, ".pdf")
            if (result != null) {
                selectedFilePath = result.second
                selectedFileType = "PDF"
            }
        }
    }

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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Upload Prescription", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Send directly to verified pharmacist for verification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))

                // Pharmacy selector
                Text("Select Pharmacy:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(selectedPharmacy.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(selectedPharmacy.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Upload Actions
                Text("Prescription File:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val (uri, path) = DocumentStorageHelper.createTempCameraImageUri(context)
                            tempCameraPath = path
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { pdfLauncher.launch("application/pdf") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prescription File Preview
                if (selectedFilePath != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (selectedFileType == "IMAGE") {
                            val bitmap = remember(selectedFilePath) {
                                try {
                                    BitmapFactory.decodeFile(selectedFilePath)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Prescription Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Image selected (${File(selectedFilePath!!).name})", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("PDF Prescription Attached", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No file attached yet. Take photo or choose image/PDF.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Patient Note Input
                MediOutlinedTextField(
                    value = patientNote,
                    onValueChange = { patientNote = it },
                    label = { Text("Patient Note (Optional)") },
                    placeholder = { Text("e.g. Please provide all medicines mentioned in this prescription.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSubmit(
                                selectedPharmacy.id,
                                selectedPharmacy.name,
                                selectedFilePath ?: "",
                                selectedFileType,
                                patientNote
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MediTeal, contentColor = Color.White),
                        modifier = Modifier.weight(1f).testTag("btn_send_to_pharmacist")
                    ) {
                        Text("Send Request", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Patient Clarification Dialog
// -------------------------------------------------------------
@Composable
fun PatientClarificationDialog(
    order: PrescriptionOrder,
    onDismiss: () -> Unit,
    onSubmit: (replyMessage: String, newAttachmentPath: String?) -> Unit
) {
    val context = LocalContext.current
    var replyText by remember { mutableStateOf("") }
    var newFilePath by remember { mutableStateOf<String?>(null) }
    var tempCameraPath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraPath != null) {
            newFilePath = tempCameraPath
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = DocumentStorageHelper.saveFileToPrivateStorage(context, uri, ".jpg")
            if (result != null) {
                newFilePath = result.second
            }
        }
    }

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
                Text("Prescription Clarification", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Pharmacist message history
                Text("CLARIFICATION HISTORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                order.clarifications.forEach { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.senderRole == "PHARMACIST") MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(msg.senderName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text(msg.message, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Upload Clearer Prescription (Optional):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val (uri, path) = DocumentStorageHelper.createTempCameraImageUri(context)
                            tempCameraPath = path
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery", fontSize = 11.sp)
                    }
                }

                if (newFilePath != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("✓ New file attached: ${File(newFilePath!!).name}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                MediOutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Your Response to Pharmacist") },
                    placeholder = { Text("e.g. Uploaded clearer photo with 500mg strength visible.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSubmit(replyText, newFilePath) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Patient Payment Dialog
// -------------------------------------------------------------
@Composable
fun PatientPaymentDialog(
    order: PrescriptionOrder,
    onDismiss: () -> Unit,
    onPaymentSuccess: (paymentMethod: String) -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf("UPI / Google Pay") }
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
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
                Text("Confirm & Pay", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Order #${order.id} • ${order.pharmacyName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))

                Text("VERIFIED MEDICINES:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                order.verifiedMedicines.forEach { med ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(med.medicineName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Qty: ${med.quantity} • ${med.form} (${med.strength})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("₹${(med.price * med.quantity).toInt()}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Delivery Fee", style = MaterialTheme.typography.bodySmall)
                    Text("₹${order.deliveryFee.toInt()}", style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated Delivery", style = MaterialTheme.typography.bodySmall)
                    Text("~${order.estimatedDeliveryMinutes} minutes", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Payable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("₹${order.totalAmount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("SELECT PAYMENT METHOD:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                listOf("UPI / Google Pay", "Credit / Debit Card", "Cash on Delivery (COD)").forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = method }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == method,
                            onClick = { selectedPaymentMethod = method }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(method, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), enabled = !isProcessing) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            isProcessing = true
                            onPaymentSuccess(selectedPaymentMethod)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Pay ₹${order.totalAmount.toInt()}")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Prescription Order Details Dialog
// -------------------------------------------------------------
@Composable
fun PrescriptionOrderDetailsDialog(
    order: PrescriptionOrder,
    onDismiss: () -> Unit,
    onClarify: () -> Unit,
    onPay: () -> Unit
) {
    val context = LocalContext.current
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Request #${order.id}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(order.pharmacyName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    StatusBadge(status = order.status)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prescription Image / PDF preview
                if (order.prescriptionFilePath.isNotBlank()) {
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
                                    .height(160.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Prescription",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { DocumentStorageHelper.openPdfWithViewer(context, order.prescriptionFilePath) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Attached PDF Prescription")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (order.verifiedMedicines.isNotEmpty()) {
                    Text("VERIFIED MEDICINES LIST:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    order.verifiedMedicines.forEach { med ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(med.medicineName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Qty: ${med.quantity} • ${med.form} (${med.strength})", style = MaterialTheme.typography.labelSmall)
                            }
                            if (med.isInStock) {
                                Text("₹${(med.price * med.quantity).toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Unavailable", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (order.status == PrescriptionOrderStatus.OUT_FOR_DELIVERY || order.status == PrescriptionOrderStatus.PREPARING) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("DELIVERY CONFIRMATION OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(order.deliveryOtp, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                            Text("Share this with the delivery partner upon arrival", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (order.status == PrescriptionOrderStatus.CLARIFICATION_REQUIRED) {
                    Button(onClick = onClarify, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Default.Reply, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Respond to Pharmacist")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else if (order.status == PrescriptionOrderStatus.PRESCRIPTION_VERIFIED) {
                    Button(onClick = onPay, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm & Pay ₹${order.totalAmount.toInt()}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Location Detection Banner
// -------------------------------------------------------------
@Composable
fun LocationDetectionBanner(
    localityName: String,
    permissionGranted: Boolean,
    isGpsEnabled: Boolean,
    onRequestPermission: () -> Unit
) {
    if (!permissionGranted) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Allow location access to find medical shops near you.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Allow Location", fontSize = 11.sp)
                }
            }
        }
    } else if (!isGpsEnabled) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.GpsOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Unable to determine your location. Please turn on GPS.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    } else {
        // Location Active Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nearby $localityName",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Auto-detected Location",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// -------------------------------------------------------------
// Nearby Pharmacies List
// -------------------------------------------------------------
@Composable
fun NearbyPharmaciesList(
    nearbyState: ApiResult<List<PharmacyDetailsDto>>,
    locality: String,
    onSelectPharmacy: (PharmacyDetailsDto) -> Unit,
    onOpenMaps: () -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Nearby Medical Shops",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onOpenMaps) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View on Maps", fontSize = 12.sp)
            }
        }

        when (nearbyState) {
            is ApiResult.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ApiResult.HttpError -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nearbyState.userMessage, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRetry) { Text("Retry") }
                    }
                }
            }
            is ApiResult.NetworkError -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nearbyState.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRetry) { Text("Retry") }
                    }
                }
            }
            is ApiResult.Unconfigured -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Pharmacy service unconfigured", style = MaterialTheme.typography.bodyMedium)
                }
            }
            is ApiResult.Success -> {
                val pharmacies = nearbyState.data
                if (pharmacies.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No medical shops found near $locality")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(pharmacies, key = { it.id }) { pharmacy ->
                            PharmacyCard(pharmacy = pharmacy, onClick = { onSelectPharmacy(pharmacy) })
                        }
                        item {
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Premium Pharmacy Open / Closed Status Badge
// -------------------------------------------------------------
@Composable
fun PharmacyOpenClosedBadge(
    isOpen: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isOpen) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
    val borderColor = if (isOpen) Color(0xFF6EE7B7) else Color(0xFFFCA5A5)
    val textColor = if (isOpen) Color(0xFF047857) else Color(0xFFB91C1C)
    val labelText = if (isOpen) "● Open Now" else "● Closed"

    Surface(
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = labelText,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PharmacyCard(
    pharmacy: PharmacyDetailsDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("pharmacy_card_${pharmacy.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pharmacy.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", pharmacy.distanceKm)} km • ${pharmacy.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                PharmacyOpenClosedBadge(isOpen = pharmacy.isOpen)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pharmacy.deliveryAvailable) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Delivery Available",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            "In-store pickup only",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_view_details_${pharmacy.id}")
                ) {
                    Text("View Details", fontSize = 12.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Pharmacy Details View
// -------------------------------------------------------------
@Composable
fun PharmacyDetailsView(
    pharmacy: PharmacyDetailsDto,
    onBack: () -> Unit,
    onUploadPrescriptionForPharmacy: () -> Unit,
    onSelectMedicine: (PharmacyMedicineItemDto) -> Unit,
    onOpenMapsDirections: () -> Unit,
    onCallPharmacy: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pharmacy.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        PharmacyOpenClosedBadge(isOpen = pharmacy.isOpen)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(pharmacy.address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Working Hours: ${pharmacy.workingHours}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onOpenMapsDirections,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        if (!pharmacy.phoneNumber.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = { onCallPharmacy(pharmacy.phoneNumber) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onUploadPrescriptionForPharmacy,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MediTeal, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Prescription to this Pharmacy", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        item {
            Text("Available Medicines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (pharmacy.medicines.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Inventory not published online by this medical shop.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Please upload prescription or call for direct inquiries.", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } else {
            items(pharmacy.medicines, key = { it.id }) { med ->
                PharmacyMedicineItemRow(medicine = med, onSelect = { onSelectMedicine(med) })
            }
        }
    }
}

@Composable
fun PharmacyMedicineItemRow(
    medicine: PharmacyMedicineItemDto,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(medicine.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${medicine.dosageForm} • ${if (medicine.isInStock) "In Stock" else "Out of Stock"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (medicine.isInStock) Color(0xFF047857) else Color(0xFFB91C1C)
                )
            }

            if (medicine.price != null) {
                Text(
                    "₹${medicine.price.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Price Comparison View
// -------------------------------------------------------------
@Composable
fun PriceComparisonView(
    medicine: MedicineCatalogDto,
    offersState: ApiResult<List<PharmacyOfferDto>>,
    sortOption: PharmacySortOption,
    onSortChanged: (PharmacySortOption) -> Unit,
    onBack: () -> Unit,
    onOpenMaps: () -> Unit,
    onOrder: (PharmacyOfferDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(medicine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Generic: ${medicine.genericName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sort Options
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = sortOption == PharmacySortOption.NEAREST_DISTANCE,
                onClick = { onSortChanged(PharmacySortOption.NEAREST_DISTANCE) },
                label = { Text("Nearest", fontSize = 11.sp) }
            )
            FilterChip(
                selected = sortOption == PharmacySortOption.LOWEST_PRICE,
                onClick = { onSortChanged(PharmacySortOption.LOWEST_PRICE) },
                label = { Text("Lowest Price", fontSize = 11.sp) }
            )
            FilterChip(
                selected = sortOption == PharmacySortOption.FASTEST_DELIVERY,
                onClick = { onSortChanged(PharmacySortOption.FASTEST_DELIVERY) },
                label = { Text("Fastest", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (offersState) {
            is ApiResult.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ApiResult.Success -> {
                val offers = when (sortOption) {
                    PharmacySortOption.NEAREST_DISTANCE -> offersState.data.sortedBy { it.distanceKm }
                    PharmacySortOption.LOWEST_PRICE -> offersState.data.sortedBy { it.price }
                    PharmacySortOption.FASTEST_DELIVERY -> offersState.data.sortedBy { it.estimatedDeliveryMinutes }
                }

                if (offers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No pricing available for this medicine.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onOpenMaps) { Text("Search nearby in Google Maps") }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(offers, key = { it.pharmacyId }) { offer ->
                            PharmacyOfferCard(offer = offer, onOrder = { onOrder(offer) })
                        }
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Comparison service temporarily unavailable.")
                }
            }
        }
    }
}

@Composable
fun PharmacyOfferCard(
    offer: PharmacyOfferDto,
    onOrder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(offer.pharmacyName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${String.format(Locale.getDefault(), "%.1f", offer.distanceKm)} km away • ~${offer.estimatedDeliveryMinutes} mins", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (offer.isInStock) "In Stock" else "Out of Stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (offer.isInStock) Color(0xFF047857) else Color(0xFFB91C1C),
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("₹${offer.price.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onOrder,
                    enabled = offer.isInStock,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Order", fontSize = 12.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Medicine Search Results List
// -------------------------------------------------------------
@Composable
fun MedicineSearchResultsList(
    viewModel: MedicinePharmacyViewModel,
    searchQuery: String,
    onSelectMedicine: (MedicineCatalogDto) -> Unit,
    onOpenMaps: () -> Unit
) {
    val catalogState by viewModel.medicineCatalogState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Search Results for \"$searchQuery\"", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            TextButton(onClick = onOpenMaps) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Search in Maps", fontSize = 12.sp)
            }
        }

        when (val state = catalogState) {
            is ApiResult.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ApiResult.Success -> {
                val meds = state.data
                if (meds.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No medicines matched \"$searchQuery\"")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onOpenMaps) { Text("Search nearby in Google Maps") }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(meds, key = { it.id }) { med ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectMedicine(med) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(med.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("${med.form} • ${med.strength}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Medicine catalog service temporarily unavailable.")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Order Confirmation Dialog for Direct Offers
// -------------------------------------------------------------
@Composable
fun OrderConfirmationDialog(
    offer: PharmacyOfferDto,
    medicineName: String,
    requiresPrescription: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Medicine Order") },
        text = {
            Column {
                Text("Medicine: $medicineName", fontWeight = FontWeight.Bold)
                Text("Pharmacy: ${offer.pharmacyName}")
                Text("Price: ₹${offer.price.toInt()}")
                Text("Estimated Delivery: ~${offer.estimatedDeliveryMinutes} mins")
                if (requiresPrescription) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⚠️ This is a scheduled prescription medicine. The pharmacy will verify your prescription before final dispatch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Place Order Request")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

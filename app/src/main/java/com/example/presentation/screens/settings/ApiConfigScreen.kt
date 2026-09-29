package com.example.presentation.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.core.config.ApiConfig
import com.example.core.security.AuthUser
import com.example.core.security.UserRole
import com.example.presentation.common.*
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.MyDocumentsViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiConfigScreen(
    authViewModel: AuthViewModel,
    currentUser: AuthUser?,
    documentsViewModel: MyDocumentsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activeShares by documentsViewModel.activeShares.collectAsState()
    val allShares by documentsViewModel.allShares.collectAsState()
    val accessLogs by documentsViewModel.accessLogs.collectAsState()
    val emergencyInfo by documentsViewModel.emergencyInfo.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    var selectedPrivacyTab by remember { mutableIntStateOf(0) } // 0: Active Access, 1: Consent History, 2: Sharing Preferences
    var appLockEnabled by remember { mutableStateOf(false) }
    var biometricLockEnabled by remember { mutableStateOf(true) }
    var autoLockTimeout by remember { mutableStateOf("1 Minute") }
    var requireDurationPreference by remember { mutableStateOf(true) }
    var notifyOnAccessPreference by remember { mutableStateOf(true) }

    var baseUrl by remember { mutableStateOf(ApiConfig.getBaseUrl(context)) }
    var hospitalUrl by remember { mutableStateOf(ApiConfig.getCustomEndpoint(context, ApiConfig.ENDPOINT_HOSPITAL)) }
    var doctorUrl by remember { mutableStateOf(ApiConfig.getCustomEndpoint(context, ApiConfig.ENDPOINT_DOCTOR)) }
    var appointmentUrl by remember { mutableStateOf(ApiConfig.getCustomEndpoint(context, ApiConfig.ENDPOINT_APPOINTMENT)) }
    var queueUrl by remember { mutableStateOf(ApiConfig.getCustomEndpoint(context, ApiConfig.ENDPOINT_QUEUE)) }
    var prescriptionUrl by remember { mutableStateOf(ApiConfig.getCustomEndpoint(context, ApiConfig.ENDPOINT_PRESCRIPTION)) }
    var ocrUrl by remember { mutableStateOf(ApiConfig.getCustomEndpoint(context, ApiConfig.ENDPOINT_OCR)) }
    var aiHealthUrl by remember { mutableStateOf(ApiConfig.getCustomEndpoint(context, ApiConfig.ENDPOINT_AI_HEALTH)) }
    var pharmacyUrl by remember { mutableStateOf(ApiConfig.getCustomEndpoint(context, ApiConfig.ENDPOINT_PHARMACY)) }
    var orderUrl by remember { mutableStateOf(ApiConfig.getCustomEndpoint(context, ApiConfig.ENDPOINT_ORDER)) }

    var saveStatus by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Account, privacy, security & app preferences",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MediTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(MediSpacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
        ) {
            // Profile Card with MediBridge Logo
            MediCard(
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_medibridge_logo),
                        contentDescription = "MediBridge Profile Photo",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(MediCornerRadius.md))
                            .testTag("settings_profile_photo")
                    )
                    Spacer(modifier = Modifier.width(MediSpacing.md))
                    Column {
                        Text(
                            text = currentUser?.fullName ?: "MediBridge User",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentUser?.email ?: "user@medibridge.org",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        UserRoleChip(currentUser?.role ?: UserRole.PATIENT)
                    }
                }
            }

            // Privacy & Consent Section (Problem Statement 3)
            MediSectionHeader(
                title = "Privacy & Consent",
                subtitle = "Patient-held active access, consent history, and sharing preferences",
                accentColor = MediTeal
            )

            MediCard(
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Privacy & Consent Sub-tabs: Active Access | Consent History | Preferences
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedPrivacyTab == 0,
                            onClick = { selectedPrivacyTab = 0 },
                            label = { Text("Active Access (${activeShares.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MediTealLight,
                                selectedLabelColor = MediTealDark
                            )
                        )
                        FilterChip(
                            selected = selectedPrivacyTab == 1,
                            onClick = { selectedPrivacyTab = 1 },
                            label = { Text("Consent History", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MediTealLight,
                                selectedLabelColor = MediTealDark
                            )
                        )
                        FilterChip(
                            selected = selectedPrivacyTab == 2,
                            onClick = { selectedPrivacyTab = 2 },
                            label = { Text("Preferences", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MediTealLight,
                                selectedLabelColor = MediTealDark
                            )
                        )
                    }

                    when (selectedPrivacyTab) {
                        0 -> {
                            // 1. ACTIVE ACCESS & REVOKE
                            if (activeShares.isEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(MediCornerRadius.md),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "No Active Healthcare Shares",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Your health records are stored privately on this device only. When you grant temporary access to a doctor, hospital, or pharmacist in My Health Records, it will appear here and can be revoked at any time.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    activeShares.forEach { share ->
                                        val expiryText = if (share.accessDuration == "Until Revoked" || share.expiresAt == Long.MAX_VALUE) {
                                            "Continuous until revoked"
                                        } else {
                                            "Expires: ${dateFormat.format(Date(share.expiresAt))}"
                                        }

                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                            shape = RoundedCornerShape(MediCornerRadius.md),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                        Icon(
                                                            Icons.Default.FolderShared,
                                                            contentDescription = null,
                                                            tint = MediTeal,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = share.documentName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }

                                                    Surface(
                                                        color = Color(0xFFE0F2FE),
                                                        shape = RoundedCornerShape(MediCornerRadius.pill)
                                                    ) {
                                                        Text(
                                                            text = share.recipientType,
                                                            color = Color(0xFF0369A1),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Recipient: ${share.recipientName} (${share.purpose})",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Access Duration: ${share.accessDuration} • $expiryText",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                OutlinedButton(
                                                    onClick = {
                                                        documentsViewModel.revokeShare(share.id, share.recipientName)
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                    border = BorderStroke(1.dp, Color(0xFFDC2626)),
                                                    shape = RoundedCornerShape(MediCornerRadius.sm),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier
                                                        .align(Alignment.End)
                                                        .testTag("btn_revoke_share_${share.id}")
                                                ) {
                                                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFDC2626))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Revoke Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // 2. CONSENT HISTORY (Revoked & Expired Grants)
                            val pastShares = remember(allShares) {
                                allShares.filter { it.status.uppercase() != "ACTIVE" }
                            }
                            if (pastShares.isEmpty() && accessLogs.none { it.action == "REVOKED" }) {
                                Text(
                                    "No revoked or expired consent records yet. All previous revocations are immutably logged here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    pastShares.forEach { past ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(MediCornerRadius.sm),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(past.documentName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("Recipient: ${past.recipientName} (${past.recipientType})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("Revoked on: ${dateFormat.format(Date(past.sharedAt))}", fontSize = 10.sp, color = Color(0xFFDC2626))
                                                }
                                                Surface(
                                                    color = Color(0xFFFEE2E2),
                                                    shape = RoundedCornerShape(MediCornerRadius.pill)
                                                ) {
                                                    Text("REVOKED", color = Color(0xFF991B1B), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // 3. SHARING PREFERENCES
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Require Duration On All Shares", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Force an expiry time limit for every shared record", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = requireDurationPreference,
                                        onCheckedChange = { requireDurationPreference = it }
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Emergency Responder Access", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Allow paramedics instant access to Blood Group & Allergies", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = emergencyInfo?.isSharingAllowedInEmergency ?: true,
                                        onCheckedChange = { documentsViewModel.toggleEmergencySharing(it) }
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Access Audit Notifications", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Alert on phone whenever doctor or pharmacy views a record", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = notifyOnAccessPreference,
                                        onCheckedChange = { notifyOnAccessPreference = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Security Options (App Lock & Biometric)
            MediSectionHeader(
                title = "Security & Encryption",
                subtitle = "App Lock, biometric protection, and local AES-256 storage",
                accentColor = Color(0xFF6366F1)
            )

            MediCard(
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Pin, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("App PIN Lock", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Require 4-digit PIN before opening Health Vault", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = appLockEnabled,
                            onCheckedChange = { appLockEnabled = it }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Biometric Lock (Fingerprint / Face)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Fast biometric unlock supported by hardware", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = biometricLockEnabled,
                            onCheckedChange = { biometricLockEnabled = it }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Lock Timeout", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Lock vault when leaving the application", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(MediCornerRadius.pill)
                        ) {
                            Text(
                                text = autoLockTimeout,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(MediCornerRadius.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AES-256 On-Device Vault Key Active • Zero cloud leak",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3730A3)
                            )
                        }
                    }
                }
            }

            // Architecture Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(MediSpacing.md)) {
                    Text(
                        "API-First Architecture",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MediTeal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "MediBridge connects to your real backend endpoints. Leave blank or configure custom URLs to test live integrations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            MediSectionHeader(
                title = "Backend Server URL",
                accentColor = MediTeal
            )

            MediOutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Master Base URL (e.g. https://api.medibridge.org)") },
                modifier = Modifier.fillMaxWidth().testTag("input_base_url"),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )

            MediSectionHeader(
                title = "Individual Service Endpoints (Optional)",
                subtitle = "Custom routes for microservices",
                accentColor = MediTeal
            )

            MediOutlinedTextField(
                value = hospitalUrl,
                onValueChange = { hospitalUrl = it },
                label = { Text("Hospital Discovery URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )
            MediOutlinedTextField(
                value = doctorUrl,
                onValueChange = { doctorUrl = it },
                label = { Text("Doctor & Rosters URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )
            MediOutlinedTextField(
                value = appointmentUrl,
                onValueChange = { appointmentUrl = it },
                label = { Text("Appointment Booking URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )
            MediOutlinedTextField(
                value = queueUrl,
                onValueChange = { queueUrl = it },
                label = { Text("Live Queue Telemetry URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )
            MediOutlinedTextField(
                value = prescriptionUrl,
                onValueChange = { prescriptionUrl = it },
                label = { Text("Prescription Service URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )
            MediOutlinedTextField(
                value = ocrUrl,
                onValueChange = { ocrUrl = it },
                label = { Text("OCR Vision URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )
            MediOutlinedTextField(
                value = aiHealthUrl,
                onValueChange = { aiHealthUrl = it },
                label = { Text("AI Health / Gemini URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )
            MediOutlinedTextField(
                value = pharmacyUrl,
                onValueChange = { pharmacyUrl = it },
                label = { Text("Pharmacy Inventory URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )
            MediOutlinedTextField(
                value = orderUrl,
                onValueChange = { orderUrl = it },
                label = { Text("Order & Delivery URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )

            if (saveStatus != null) {
                MediStatusBadge(
                    statusText = saveStatus!!,
                    statusType = MediStatusType.SUCCESS
                )
            }

            Spacer(modifier = Modifier.height(MediSpacing.xs))

            MediPrimaryButton(
                text = "Save Configuration",
                onClick = {
                    ApiConfig.setBaseUrl(context, baseUrl)
                    ApiConfig.setCustomEndpoint(context, ApiConfig.ENDPOINT_HOSPITAL, hospitalUrl)
                    ApiConfig.setCustomEndpoint(context, ApiConfig.ENDPOINT_DOCTOR, doctorUrl)
                    ApiConfig.setCustomEndpoint(context, ApiConfig.ENDPOINT_APPOINTMENT, appointmentUrl)
                    ApiConfig.setCustomEndpoint(context, ApiConfig.ENDPOINT_QUEUE, queueUrl)
                    ApiConfig.setCustomEndpoint(context, ApiConfig.ENDPOINT_PRESCRIPTION, prescriptionUrl)
                    ApiConfig.setCustomEndpoint(context, ApiConfig.ENDPOINT_OCR, ocrUrl)
                    ApiConfig.setCustomEndpoint(context, ApiConfig.ENDPOINT_AI_HEALTH, aiHealthUrl)
                    ApiConfig.setCustomEndpoint(context, ApiConfig.ENDPOINT_PHARMACY, pharmacyUrl)
                    ApiConfig.setCustomEndpoint(context, ApiConfig.ENDPOINT_ORDER, orderUrl)
                    saveStatus = "Endpoints saved successfully!"
                },
                icon = Icons.Default.Save,
                testTag = "btn_save_api_config"
            )

            Spacer(modifier = Modifier.height(MediSpacing.xs))

            // Logout Option
            MediDestructiveButton(
                text = "Sign Out (${currentUser?.fullName ?: "User"})",
                onClick = {
                    authViewModel.logout()
                    onBack()
                },
                icon = Icons.Default.Logout,
                testTag = "btn_logout"
            )

            Spacer(modifier = Modifier.height(MediSpacing.xl))
        }
    }
}

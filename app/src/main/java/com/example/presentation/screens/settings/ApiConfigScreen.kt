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
import com.example.R
import com.example.core.config.ApiConfig
import com.example.core.security.AuthUser
import com.example.core.security.UserRole
import com.example.presentation.common.*
import com.example.presentation.viewmodel.AuthViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiConfigScreen(
    authViewModel: AuthViewModel,
    currentUser: AuthUser?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
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
                            text = "Settings & Gateway",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Account, endpoints and app preferences",
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

package com.example.presentation.screens.settings

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.config.ApiConfig
import com.example.core.security.AuthUser
import com.example.core.security.UserRole
import com.example.presentation.viewmodel.AuthViewModel

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
                title = { Text("API & Gateway Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Card with MediBridge Logo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_medibridge_logo),
                        contentDescription = "MediBridge Profile Photo",
                        modifier = Modifier
                            .size(60.dp)
                            .testTag("settings_profile_photo")
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = currentUser?.fullName ?: "MediBridge User",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.email ?: "user@medibridge.org",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Role: ${currentUser?.role?.displayName ?: "Patient"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("API-First Architecture", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "MediBridge connects to your real backend endpoints. Leave blank or configure custom URLs to test live integrations.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Master Base URL (e.g. https://api.medibridge.org)") },
                modifier = Modifier.fillMaxWidth().testTag("input_base_url")
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("INDIVIDUAL ENDPOINT OVERRIDES (OPTIONAL)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = hospitalUrl, onValueChange = { hospitalUrl = it }, label = { Text("Hospital Discovery URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = doctorUrl, onValueChange = { doctorUrl = it }, label = { Text("Doctor & Rosters URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = appointmentUrl, onValueChange = { appointmentUrl = it }, label = { Text("Appointment Booking URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = queueUrl, onValueChange = { queueUrl = it }, label = { Text("Live Queue Telemetry URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = prescriptionUrl, onValueChange = { prescriptionUrl = it }, label = { Text("Prescription Service URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = ocrUrl, onValueChange = { ocrUrl = it }, label = { Text("OCR Vision URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = aiHealthUrl, onValueChange = { aiHealthUrl = it }, label = { Text("AI Health / Gemini URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = pharmacyUrl, onValueChange = { pharmacyUrl = it }, label = { Text("Pharmacy Inventory URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = orderUrl, onValueChange = { orderUrl = it }, label = { Text("Order & Delivery URL") }, modifier = Modifier.fillMaxWidth())

            if (saveStatus != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(saveStatus!!, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
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
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_save_api_config")
            ) {
                Text("Save Configuration")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Option
            OutlinedButton(
                onClick = {
                    authViewModel.logout()
                    onBack()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().testTag("btn_logout")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out (${currentUser?.fullName ?: "User"})")
            }
        }
    }
}

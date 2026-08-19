package com.example.presentation.screens.portals

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
import com.example.core.security.AuthUser
import com.example.core.security.UserRole
import com.example.presentation.common.ApiUnavailableCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolePortalScreen(
    user: AuthUser,
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

            Spacer(modifier = Modifier.height(16.dp))

            when (user.role) {
                UserRole.DOCTOR -> {
                    Text("TODAY'S CLINICAL QUEUE & DIGITAL PRESCRIPTIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    ApiUnavailableCard(
                        serviceName = "Doctor Queue Manager",
                        endpointName = "DOCTOR_API_URL",
                        onConfigureClick = onNavigateToApiConfig
                    )
                }
                UserRole.PHARMACIST -> {
                    Text("PENDING PRESCRIPTION VERIFICATIONS & DISPATCH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    ApiUnavailableCard(
                        serviceName = "Pharmacist Verification Gateway",
                        endpointName = "PHARMACY_API_URL",
                        onConfigureClick = onNavigateToApiConfig
                    )
                }
                UserRole.DELIVERY_PARTNER -> {
                    Text("ACTIVE DELIVERY DISPATCHES & OTP VERIFICATION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    ApiUnavailableCard(
                        serviceName = "Delivery Dispatcher",
                        endpointName = "DELIVERY_API_URL",
                        onConfigureClick = onNavigateToApiConfig
                    )
                }
                UserRole.HOSPITAL_ADMIN, UserRole.SUPER_ADMIN -> {
                    Text("FACILITY ROSTER, DEPARTMENTS & AUDIT LOGS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    ApiUnavailableCard(
                        serviceName = "Hospital Admin & Audit Gateway",
                        endpointName = "BASE_URL",
                        onConfigureClick = onNavigateToApiConfig
                    )
                }
                UserRole.PATIENT -> {}
            }

            Spacer(modifier = Modifier.weight(1f))

            // Demo Role Switcher to easily test different interfaces
            Text("SWITCH DEMO ROLE VIEW:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                UserRole.entries.take(3).forEach { r ->
                    OutlinedButton(
                        onClick = { onSwitchRole(r) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
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
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(r.displayName, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

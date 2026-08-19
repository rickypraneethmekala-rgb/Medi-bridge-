package com.example.presentation.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.security.AuthUser
import com.example.presentation.common.EmergencyQuickAccessBanner
import com.example.presentation.common.UserRoleChip

@Composable
fun HomeScreen(
    currentUser: AuthUser?,
    isSeniorMode: Boolean,
    onNavigateToHospitals: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToMedicines: () -> Unit,
    onNavigateToHealthAssistant: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onEmergencyTrigger: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Top Header with Profile Photo
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_medibridge_logo),
                            contentDescription = "Profile Photo - MediBridge",
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("profile_photo_medibridge")
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Welcome, ${currentUser?.fullName ?: "Patient"}",
                                style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "MediBridge Healthcare Gateway",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (currentUser != null) {
                        UserRoleChip(currentUser.role)
                    }
                }
            }
        }

        // Emergency Quick Banner
        EmergencyQuickAccessBanner(onEmergencyClick = onEmergencyTrigger)

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Category Action Grid
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "HEALTHCARE PATHWAYS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ServiceActionCard(
                    title = "Find Hospital & Doctor",
                    subtitle = "Govt & Pvt Hospitals, live queue waiting times",
                    icon = Icons.Default.LocalHospital,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHospitals,
                    testTag = "card_find_hospitals"
                )
                ServiceActionCard(
                    title = "Book & Live Queue",
                    subtitle = "Digital tokens & live appointment tracker",
                    icon = Icons.Default.EventAvailable,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAppointments,
                    testTag = "card_appointments_queue"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ServiceActionCard(
                    title = "Digital Rx & OCR",
                    subtitle = "Prescription scan, AI explanation & voice",
                    icon = Icons.Default.DocumentScanner,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMedicines,
                    testTag = "card_prescriptions_ocr"
                )
                ServiceActionCard(
                    title = "Pharmacy & Orders",
                    subtitle = "Verified pricing, delivery & OTP",
                    icon = Icons.Default.LocalPharmacy,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMedicines,
                    testTag = "card_pharmacy_orders"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ServiceActionCard(
                    title = "Medicine Reminders",
                    subtitle = "Daily dose tracker & history",
                    icon = Icons.Default.Alarm,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReminders,
                    testTag = "card_reminders"
                )
                ServiceActionCard(
                    title = "AI Health Assistant",
                    subtitle = "Safe health education (EN/TE/HI)",
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHealthAssistant,
                    testTag = "card_ai_assistant"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ServiceActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(145.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
        }
    }
}

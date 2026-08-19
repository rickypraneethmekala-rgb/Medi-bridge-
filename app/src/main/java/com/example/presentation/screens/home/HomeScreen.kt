package com.example.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.security.AuthUser
import com.example.presentation.common.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    currentUser: AuthUser?,
    isSeniorMode: Boolean,
    onNavigateToHospitals: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToMedicines: () -> Unit,
    onNavigateToHealthAssistant: () -> Unit,
    onNavigateToMyDocuments: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onEmergencyTrigger: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Top Header with MediBridge+ Branding & Profile Photo
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MediSpacing.lg, vertical = MediSpacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_medibridge_logo),
                            contentDescription = "Profile Photo - MediBridge",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(MediCornerRadius.md))
                                .testTag("profile_photo_medibridge")
                        )
                        Spacer(modifier = Modifier.width(MediSpacing.md))
                        Column {
                            Text(
                                text = "Welcome, ${currentUser?.fullName ?: "Patient"}",
                                style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "MediBridge+ Healthcare Gateway",
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

        Spacer(modifier = Modifier.height(MediSpacing.sm))

        // Emergency Quick Banner (Red 24x7)
        EmergencyQuickAccessBanner(onEmergencyClick = onEmergencyTrigger)

        Spacer(modifier = Modifier.height(MediSpacing.md))

        // Quick Category Action Grid
        Column(modifier = Modifier.padding(horizontal = MediSpacing.lg)) {
            MediSectionHeader(
                title = "Healthcare Pathways",
                subtitle = "Unified patient access and clinical services",
                accentColor = MediTeal
            )

            Spacer(modifier = Modifier.height(MediSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MediSpacing.md)
            ) {
                ServiceActionCard(
                    title = "Find Hospital & Doctor",
                    subtitle = "Govt & Pvt Hospitals, live queue waiting times",
                    icon = Icons.Default.LocalHospital,
                    accentColor = AccentAppt,
                    accentBg = AccentApptBg,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHospitals,
                    testTag = "card_find_hospitals"
                )
                ServiceActionCard(
                    title = "Book & Live Queue",
                    subtitle = "Digital tokens & live appointment tracker",
                    icon = Icons.Default.EventAvailable,
                    accentColor = AccentAppt,
                    accentBg = AccentApptBg,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAppointments,
                    testTag = "card_appointments_queue"
                )
            }

            Spacer(modifier = Modifier.height(MediSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MediSpacing.md)
            ) {
                ServiceActionCard(
                    title = "AI Health Assistant",
                    subtitle = "Safe health education in EN, TE & HI",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = AccentAi,
                    accentBg = AccentAiBg,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHealthAssistant,
                    testTag = "card_ai_assistant"
                )
                ServiceActionCard(
                    title = "Pharmacy & Orders",
                    subtitle = "Verified pricing, delivery & prescription OTP",
                    icon = Icons.Default.LocalPharmacy,
                    accentColor = AccentMedi,
                    accentBg = AccentMediBg,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMedicines,
                    testTag = "card_pharmacy_orders"
                )
            }

            Spacer(modifier = Modifier.height(MediSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MediSpacing.md)
            ) {
                ServiceActionCard(
                    title = "Medicine Reminders",
                    subtitle = "Daily dose tracker, schedules & history",
                    icon = Icons.Default.Alarm,
                    accentColor = MediWarning,
                    accentBg = MediWarningBg,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReminders,
                    testTag = "card_reminders"
                )
                ServiceActionCard(
                    title = "My Documents",
                    subtitle = "Store prescriptions, scans & lab reports locally",
                    icon = Icons.Default.Folder,
                    accentColor = AccentDocuments,
                    accentBg = AccentDocumentsBg,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMyDocuments,
                    testTag = "card_my_documents"
                )
            }
        }

        Spacer(modifier = Modifier.height(MediSpacing.xxl))
    }
}

@Composable
fun ServiceActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    accentBg: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(MediCornerRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = MediElevation.subtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MediSpacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(MediCornerRadius.md))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(MediIconSize.md)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

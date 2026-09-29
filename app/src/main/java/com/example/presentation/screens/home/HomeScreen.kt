package com.example.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
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
import com.example.presentation.common.UserRoleChip
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    currentUser: AuthUser?,
    isSeniorMode: Boolean,
    onNavigateToFindDoctorHospital: () -> Unit,
    onNavigateToBookLiveQueue: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onNavigateToPharmacyOrders: () -> Unit,
    onNavigateToMedicineReminders: () -> Unit,
    onNavigateToMyDocuments: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .testTag("screen_home")
    ) {
        // Patient Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MediSpacing.lg, vertical = MediSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_medibridge_logo),
                        contentDescription = "MediBridge Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(MediCornerRadius.md))
                            .testTag("profile_photo_medibridge")
                    )
                    Spacer(modifier = Modifier.width(MediSpacing.md))
                    Column {
                        Text(
                            text = "Hello, ${currentUser?.fullName ?: "Patient"}",
                            style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MediTeal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Consent-Driven Healthcare Gateway",
                                style = MaterialTheme.typography.bodySmall,
                                color = MediTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                if (currentUser != null) {
                    UserRoleChip(currentUser.role)
                }
            }
        }

        Spacer(modifier = Modifier.height(MediSpacing.lg))

        // ONLY The Six Requested Feature Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MediSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
        ) {
            // 1. Find Doctor & Hospital
            HomeFeatureCard(
                title = "Find Doctor & Hospital",
                subtitle = "Search verified clinics, hospitals & specialist doctors",
                icon = Icons.Default.LocalHospital,
                iconTint = MediTeal,
                iconBg = MediTealLight,
                testTag = "home_card_find_doctor_hospital",
                onClick = onNavigateToFindDoctorHospital
            )

            // 2. Book & Live Queue
            HomeFeatureCard(
                title = "Book & Live Queue",
                subtitle = "Schedule OPD appointments & track real-time queue status",
                icon = Icons.Default.EventAvailable,
                iconTint = Color(0xFFD97706),
                iconBg = Color(0xFFFEF3C7),
                testTag = "home_card_book_live_queue",
                onClick = onNavigateToBookLiveQueue
            )

            // 3. AI Health Assistant
            HomeFeatureCard(
                title = "AI Health Assistant",
                subtitle = "Multilingual medicine guidance & report summaries",
                icon = Icons.Default.AutoAwesome,
                iconTint = Color(0xFF7C3AED),
                iconBg = Color(0xFFF3E8FF),
                testTag = "home_card_ai_assistant",
                onClick = onNavigateToAiAssistant
            )

            // 4. Pharmacy & Orders
            HomeFeatureCard(
                title = "Pharmacy & Orders",
                subtitle = "Nearby verified pharmacies, medicine orders & delivery",
                icon = Icons.Default.LocalPharmacy,
                iconTint = Color(0xFF059669),
                iconBg = Color(0xFFDCFCE7),
                testTag = "home_card_pharmacy_orders",
                onClick = onNavigateToPharmacyOrders
            )

            // 5. Medicine Reminders
            HomeFeatureCard(
                title = "Medicine Reminders",
                subtitle = "Dosage alarms, schedules & family health management",
                icon = Icons.Default.Alarm,
                iconTint = Color(0xFF2563EB),
                iconBg = Color(0xFFDBEAFE),
                testTag = "home_card_medicine_reminders",
                onClick = onNavigateToMedicineReminders
            )

            // 6. My Documents
            HomeFeatureCard(
                title = "My Documents",
                subtitle = "Patient-held health records & consent-driven sharing",
                icon = Icons.Default.FolderShared,
                iconTint = Color(0xFF0284C7),
                iconBg = Color(0xFFE0F2FE),
                testTag = "home_card_my_documents",
                onClick = onNavigateToMyDocuments
            )
        }

        Spacer(modifier = Modifier.height(MediSpacing.xxl))
    }
}

@Composable
private fun HomeFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(MediCornerRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MediSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconBg,
                shape = RoundedCornerShape(MediCornerRadius.md),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(MediSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(MediSpacing.sm))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Open $title",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

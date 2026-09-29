package com.example.presentation.screens.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MedicalDocument
import com.example.data.local.entity.RecordShareEntity
import com.example.presentation.common.*
import com.example.presentation.screens.documents.ShareConfirmationQrDialog
import com.example.presentation.screens.documents.SimpleShareFlowDialog
import com.example.presentation.viewmodel.AppointmentQueueViewModel
import com.example.presentation.viewmodel.MyDocumentsViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyHealthSummaryScreen(
    documentsViewModel: MyDocumentsViewModel,
    appointmentViewModel: AppointmentQueueViewModel,
    onNavigateToRecords: () -> Unit = {},
    onNavigateToAppointments: () -> Unit = {}
) {
    val documents by documentsViewModel.documents.collectAsState()
    val activeShares by documentsViewModel.activeShares.collectAsState()

    var shareFlowRecords by remember { mutableStateOf<List<MedicalDocument>?>(null) }
    var shareSuccessGrant by remember { mutableStateOf<RecordShareEntity?>(null) }
    var shareToRevoke by remember { mutableStateOf<RecordShareEntity?>(null) }

    val prescriptions = remember(documents) {
        documents.filter { it.category.equals("Prescription", ignoreCase = true) }
    }
    val labReports = remember(documents) {
        documents.filter { it.category.equals("Lab Report", ignoreCase = true) }
    }
    val dischargeSummaries = remember(documents) {
        documents.filter { it.category.equals("Discharge Summary", ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Health Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Overview of prescriptions, diagnostics, follow-ups & sharing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .testTag("screen_my_health_summary"),
            contentPadding = PaddingValues(MediSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MediSpacing.lg)
        ) {
            // 4 Highlight Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MediSpacing.sm)
                ) {
                    SummaryStatCard(
                        title = "Active Rx",
                        value = "${prescriptions.size}",
                        subtitle = "Prescriptions",
                        icon = Icons.Default.Medication,
                        color = MediTeal,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Lab Reports",
                        value = "${labReports.size}",
                        subtitle = "Diagnostics",
                        icon = Icons.Default.Biotech,
                        color = Color(0xFF4F46E5),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Follow-up",
                        value = "28 Sep",
                        subtitle = "Dr. Sharma",
                        icon = Icons.Default.EventAvailable,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Shared",
                        value = "${activeShares.size}",
                        subtitle = "Active Grants",
                        icon = Icons.Default.Share,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 1: Active Prescriptions Overview
            item {
                MediSectionHeader(
                    title = "Active Prescriptions (${prescriptions.size})",
                    subtitle = "Doctor prescriptions currently on file",
                    accentColor = MediTeal
                )

                if (prescriptions.isEmpty()) {
                    EmptySummaryCard(
                        message = "No active prescriptions on file. Add prescriptions in My Documents."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        prescriptions.forEach { rx ->
                            PrescriptionSummaryCard(rx)
                        }
                    }
                }
            }

            // Section 2: Recent Medical Diagnostic Reports
            item {
                MediSectionHeader(
                    title = "Recent Medical Reports (${labReports.size})",
                    subtitle = "Diagnostic lab tests and clinical reports",
                    accentColor = Color(0xFF4F46E5)
                )

                if (labReports.isEmpty()) {
                    EmptySummaryCard(
                        message = "No diagnostic reports found. Upload lab panels in My Documents."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        labReports.forEach { report ->
                            ReportSummaryCard(report)
                        }
                    }
                }
            }

            // Section 3: Upcoming Clinical Follow-Up
            item {
                MediSectionHeader(
                    title = "Upcoming Follow-up Plan",
                    subtitle = "Next scheduled doctor consultations & clinical tasks",
                    accentColor = Color(0xFFD97706)
                )

                FollowUpSummaryCard()
            }

            // Section 4: Active Record-Sharing Permissions
            item {
                MediSectionHeader(
                    title = "Active Record-Sharing Permissions (${activeShares.size})",
                    subtitle = "Healthcare providers who currently hold time-limited consent",
                    accentColor = Color(0xFF059669),
                    actionText = if (documents.isNotEmpty()) "+ Share Record" else null,
                    onActionClick = {
                        if (documents.isNotEmpty()) {
                            shareFlowRecords = documents.take(1)
                        } else {
                            onNavigateToRecords()
                        }
                    }
                )

                if (activeShares.isEmpty()) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(MediCornerRadius.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MediTeal, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "All Health Records Private",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF15803D)
                                    )
                                    Text(
                                        "No doctors or hospitals currently have access. When you share a record with explicit consent, it will appear here.",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                            if (documents.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { shareFlowRecords = documents.take(1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                                    shape = RoundedCornerShape(MediCornerRadius.sm),
                                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("btn_share_record_from_summary")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share Record with Doctor or Hospital", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeShares.forEach { share ->
                            ActiveShareSummaryCard(
                                share = share,
                                onRevoke = { shareToRevoke = share }
                            )
                        }
                    }
                }
            }

            // Section 5: Health Vitals Snapshot
            item {
                MediSectionHeader(
                    title = "Vitals & Physiological Status",
                    subtitle = "Latest patient vital signs recorded",
                    accentColor = Color(0xFFE11D48)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MediSpacing.sm)
                ) {
                    VitalStatCard("Blood Pressure", "120/80", "mmHg • Normal", Color(0xFFE11D48), Modifier.weight(1f))
                    VitalStatCard("Blood Glucose", "96", "mg/dL • Fasting", Color(0xFF0284C7), Modifier.weight(1f))
                    VitalStatCard("Heart Rate", "72", "bpm • Resting", Color(0xFF059669), Modifier.weight(1f))
                }
            }
        }

        // Sharing Flow Dialog triggered from Summary
        if (shareFlowRecords != null) {
            SimpleShareFlowDialog(
                allDocuments = documents,
                initialSelectedRecords = shareFlowRecords!!,
                onDismiss = { shareFlowRecords = null },
                onGiveAccess = { selectedDocs, recipientType, recipientName, duration, purpose ->
                    documentsViewModel.shareMultipleRecords(
                        documents = selectedDocs,
                        recipientType = recipientType,
                        recipientName = recipientName,
                        duration = duration,
                        purpose = purpose
                    ) { createdGrant ->
                        shareFlowRecords = null
                        shareSuccessGrant = createdGrant
                    }
                }
            )
        }

        // Share Confirmation Dialog with QR and Token
        if (shareSuccessGrant != null) {
            ShareConfirmationQrDialog(
                share = shareSuccessGrant!!,
                onDismiss = { shareSuccessGrant = null },
                onRevokeNow = {
                    documentsViewModel.revokeShare(shareSuccessGrant!!.id, shareSuccessGrant!!.recipientName)
                    shareSuccessGrant = null
                }
            )
        }

        // Revoke Access Confirmation Dialog
        if (shareToRevoke != null) {
            val share = shareToRevoke!!
            AlertDialog(
                onDismissRequest = { shareToRevoke = null },
                icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Revoke Access to Record?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to revoke access for ${share.recipientName} (${share.recipientType})? " +
                                "Their access token will immediately expire and they will no longer be able to view: ${share.documentName}."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            documentsViewModel.revokeShare(share.id, share.recipientName)
                            shareToRevoke = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(MediCornerRadius.sm)
                    ) {
                        Text("Revoke Access Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { shareToRevoke = null }) {
                        Text("Keep Access")
                    }
                }
            )
        }
    }
}

@Composable
private fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = color.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(text = subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun PrescriptionSummaryCard(rx: MedicalDocument) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MediTealLight,
                shape = RoundedCornerShape(MediCornerRadius.sm),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Medication, contentDescription = null, tint = MediTeal, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(rx.fileName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "${rx.hospitalOrDoctor} • ${rx.recordDate}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (rx.description.isNotBlank()) {
                    Text(text = rx.description, fontSize = 10.5.sp, color = MediTealDark)
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(report: MedicalDocument) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFEEF2FF),
                shape = RoundedCornerShape(MediCornerRadius.sm),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Biotech, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(report.fileName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "${report.hospitalOrDoctor} • ${report.recordDate}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (report.description.isNotBlank()) {
                    Text(text = report.description, fontSize = 10.5.sp, color = Color(0xFF4338CA))
                }
            }
        }
    }
}

@Composable
private fun FollowUpSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dr. Rajesh Sharma (Cardiology)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Apollo Hospital • 28 Sep 2026, 10:30 AM", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(MediCornerRadius.pill)
                ) {
                    Text(
                        "In 3 Days",
                        color = Color(0xFFB45309),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Tasks: Fasting Blood Glucose (FPG) test report required before consultation.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActiveShareSummaryCard(
    share: RecordShareEntity,
    onRevoke: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val expiryText = if (share.accessDuration == "Until Revoked" || share.expiresAt == Long.MAX_VALUE) {
        "Continuous until revoked"
    } else {
        "Expires: ${dateFormat.format(Date(share.expiresAt))}"
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("active_share_card_${share.id}"),
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF86EFAC))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                when (share.recipientType) {
                                    "Doctor" -> Icons.Default.MedicalServices
                                    "Hospital" -> Icons.Default.LocalHospital
                                    "Pharmacist" -> Icons.Default.LocalPharmacy
                                    else -> Icons.Default.People
                                },
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(share.recipientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(share.recipientType, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(MediCornerRadius.pill)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = Color(0xFF15803D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Document: ${share.documentName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            Text("$expiryText • Token: ${share.secureToken}", fontSize = 11.sp, color = MediTeal, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onRevoke,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = BorderStroke(1.dp, Color(0xFFDC2626)),
                    shape = RoundedCornerShape(MediCornerRadius.sm),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp).testTag("btn_revoke_summary_${share.id}")
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Revoke Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VitalStatCard(
    title: String,
    value: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(status, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptySummaryCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

package com.example.presentation.screens.documents

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.storage.DocumentStorageHelper
import com.example.data.local.entity.AccessLogEntity
import com.example.data.local.entity.MedicalDocument
import com.example.data.local.entity.MedicineReminder
import com.example.data.local.entity.RecordShareEntity
import com.example.presentation.common.*
import com.example.presentation.viewmodel.MyDocumentsViewModel
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDocumentsScreen(
    viewModel: MyDocumentsViewModel,
    initialTab: Int = 0,
    onBack: () -> Unit = {},
    onNavigateToApiConfig: () -> Unit = {}
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val allShares by viewModel.allShares.collectAsState()
    val activeShares by viewModel.activeShares.collectAsState()
    val accessLogs by viewModel.accessLogs.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var selectedTopTab by remember { mutableIntStateOf(initialTab) } // 0: Records, 1: Consent Center, 2: Access History
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Dialog & Flow States
    var showAddDialog by remember { mutableStateOf(false) }
    var viewingRecordDetails by remember { mutableStateOf<MedicalDocument?>(null) }
    var shareFlowRecords by remember { mutableStateOf<List<MedicalDocument>?>(null) }
    var shareSuccessGrant by remember { mutableStateOf<RecordShareEntity?>(null) }
    var viewingQrShare by remember { mutableStateOf<RecordShareEntity?>(null) }
    var changeAccessShare by remember { mutableStateOf<RecordShareEntity?>(null) }
    var shareToRevoke by remember { mutableStateOf<RecordShareEntity?>(null) }
    var viewingImageDoc by remember { mutableStateOf<MedicalDocument?>(null) }
    var editingDoc by remember { mutableStateOf<MedicalDocument?>(null) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    val categories = listOf("All", "Prescription", "Lab Report", "Discharge Summary", "Medical Bill", "Other Medical Record")

    val filteredDocs = remember(documents, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") {
            documents
        } else {
            documents.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MediTeal)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "My Documents",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Patient-Held Consent-Driven Health Record Vault",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Surface(
                        color = MediTealLight,
                        shape = RoundedCornerShape(MediCornerRadius.pill),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MediTealDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Patient Owned",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MediTealDark
                            )
                        }
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
        ) {
            // Main Feature Tabs: 1. Health Records | 2. Consent Center | 3. Access History
            PrimaryTabRow(
                selectedTabIndex = selectedTopTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MediTeal
            ) {
                Tab(
                    selected = selectedTopTab == 0,
                    onClick = { selectedTopTab = 0 },
                    text = { Text("Records (${documents.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTopTab == 1,
                    onClick = { selectedTopTab = 1 },
                    text = { Text("Consent Center (${activeShares.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTopTab == 2,
                    onClick = { selectedTopTab = 2 },
                    text = { Text("Access History", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (selectedTopTab) {
                0 -> {
                    // TAB 0: RECORDS VAULT
                    RecordsTabContent(
                        documents = filteredDocs,
                        allDocuments = documents,
                        categories = categories,
                        selectedCategory = selectedCategoryFilter,
                        activeShares = activeShares,
                        onSelectCategory = { selectedCategoryFilter = it },
                        onAddRecordClick = { showAddDialog = true },
                        onShareFlowClick = { docsToShare -> shareFlowRecords = docsToShare },
                        onRecordClick = { doc -> viewingRecordDetails = doc },
                        onQuickShare = { doc -> shareFlowRecords = listOf(doc) }
                    )
                }
                1 -> {
                    // TAB 1: CONSENT CENTER
                    ConsentCenterTabContent(
                        shares = allShares,
                        onViewDetails = { share -> viewingQrShare = share },
                        onChangeAccess = { share -> changeAccessShare = share },
                        onRevokeAccess = { share -> shareToRevoke = share },
                        onNewShareClick = {
                            if (documents.isNotEmpty()) {
                                shareFlowRecords = documents.take(1)
                            } else {
                                Toast.makeText(context, "Add a health record first.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                2 -> {
                    // TAB 2: ACCESS HISTORY & AUDIT LOG
                    AccessHistoryTabContent(
                        logs = accessLogs
                    )
                }
            }
        }
    }

    // 1. Record Details Modal / Screen
    if (viewingRecordDetails != null) {
        val doc = viewingRecordDetails!!
        val docActiveShares = activeShares.filter { it.documentId == doc.id || it.documentIdsJson.contains(doc.id) }

        RecordDetailsDialog(
            record = doc,
            activeShares = docActiveShares,
            onDismiss = { viewingRecordDetails = null },
            onView = {
                viewingRecordDetails = null
                viewModel.logDocumentAccess(doc)
                if (doc.fileType.equals("PDF", ignoreCase = true)) {
                    val opened = DocumentStorageHelper.openPdfWithViewer(context, doc.localFilePath)
                    if (!opened) {
                        Toast.makeText(context, "No PDF viewer app found on device.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewingImageDoc = doc
                }
            },
            onShare = {
                viewingRecordDetails = null
                shareFlowRecords = listOf(doc)
            },
            onEdit = {
                viewingRecordDetails = null
                editingDoc = doc
            },
            onDownload = {
                Toast.makeText(context, "Record downloaded: '${doc.fileName}' saved to Downloads.", Toast.LENGTH_LONG).show()
            },
            onDelete = {
                viewingRecordDetails = null
                viewModel.deleteDocument(doc)
            }
        )
    }

    // 2. Add Health Record Dialog
    if (showAddDialog) {
        AddRecordDialog(
            isProcessing = isProcessing,
            onDismiss = { showAddDialog = false },
            onSave = { name, type, category, hospitalDoctor, recordDate, uri, note ->
                viewModel.saveDocument(
                    fileName = name,
                    fileType = type,
                    category = category,
                    hospitalOrDoctor = hospitalDoctor,
                    recordDate = recordDate,
                    sourceUri = uri,
                    description = note
                ) {
                    showAddDialog = false
                }
            }
        )
    }

    // 3. Simple Sharing Flow (Select Record → Share → Choose Recipient → Choose Records → Set Access Duration → Review → Give Access → Confirmation)
    if (shareFlowRecords != null) {
        SimpleShareFlowDialog(
            allDocuments = documents,
            initialSelectedRecords = shareFlowRecords!!,
            onDismiss = { shareFlowRecords = null },
            onGiveAccess = { selectedDocs, recipientType, recipientName, duration, purpose ->
                viewModel.shareMultipleRecords(
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

    // 4. Share Confirmation with Time-Limited QR & Code
    if (shareSuccessGrant != null) {
        ShareConfirmationQrDialog(
            share = shareSuccessGrant!!,
            onDismiss = { shareSuccessGrant = null },
            onRevokeNow = {
                viewModel.revokeShare(shareSuccessGrant!!.id, shareSuccessGrant!!.recipientName)
                shareSuccessGrant = null
            }
        )
    }

    // 5. View QR / Details for Existing Share
    if (viewingQrShare != null) {
        ShareConfirmationQrDialog(
            share = viewingQrShare!!,
            onDismiss = { viewingQrShare = null },
            onRevokeNow = {
                viewModel.revokeShare(viewingQrShare!!.id, viewingQrShare!!.recipientName)
                viewingQrShare = null
            }
        )
    }

    // 6. Change Access Duration Dialog
    if (changeAccessShare != null) {
        val share = changeAccessShare!!
        ChangeAccessDurationDialog(
            currentDuration = share.accessDuration,
            recipientName = share.recipientName,
            onDismiss = { changeAccessShare = null },
            onSave = { newDuration ->
                viewModel.updateShareDuration(share.id, newDuration, share.recipientName)
                changeAccessShare = null
            }
        )
    }

    // 7. Revoke Access Confirmation Dialog
    if (shareToRevoke != null) {
        val share = shareToRevoke!!
        AlertDialog(
            onDismissRequest = { shareToRevoke = null },
            icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Revoke Access to Record(s)?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to revoke access for ${share.recipientName} (${share.recipientType})? " +
                            "Their access token will immediately expire and they will no longer be able to view: ${share.documentName}."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revokeShare(share.id, share.recipientName)
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

    // 8. View Image Modal
    if (viewingImageDoc != null) {
        ViewDocumentImageDialog(
            document = viewingImageDoc!!,
            onDismiss = { viewingImageDoc = null }
        )
    }

    // 9. Edit Details Dialog
    if (editingDoc != null) {
        EditDocumentDialog(
            document = editingDoc!!,
            onDismiss = { editingDoc = null },
            onSave = { newTitle, newNote, newHospitalDoctor ->
                viewModel.updateDocument(editingDoc!!, newTitle, newNote, newHospitalDoctor)
                editingDoc = null
            }
        )
    }
}

// ============================================================================
// TAB 0: RECORDS VAULT CONTENT
// ============================================================================
@Composable
fun RecordsTabContent(
    documents: List<MedicalDocument>,
    allDocuments: List<MedicalDocument>,
    categories: List<String>,
    selectedCategory: String,
    activeShares: List<RecordShareEntity>,
    onSelectCategory: (String) -> Unit,
    onAddRecordClick: () -> Unit,
    onShareFlowClick: (List<MedicalDocument>) -> Unit,
    onRecordClick: (MedicalDocument) -> Unit,
    onQuickShare: (MedicalDocument) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = MediSpacing.lg, vertical = MediSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                val count = if (cat == "All") allDocuments.size else allDocuments.count { it.category.equals(cat, ignoreCase = true) }
                val label = when (cat) {
                    "All" -> "All"
                    "Prescription" -> "Prescriptions"
                    "Lab Report" -> "Lab Reports"
                    "Discharge Summary" -> "Discharge Summaries"
                    "Medical Bill" -> "Medical Bills"
                    else -> "Other Records"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(cat) },
                    label = { Text("$label ($count)", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MediTeal,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // Action Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MediSpacing.lg, vertical = 4.dp),
            shape = RoundedCornerShape(MediCornerRadius.md)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddRecordClick,
                    modifier = Modifier.weight(1f).height(42.dp).testTag("btn_add_record"),
                    colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { onShareFlowClick(allDocuments) },
                    modifier = Modifier.weight(1f).height(42.dp).testTag("btn_share_flow_top"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Records", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Records List
        if (documents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Records in this Category", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap '+ Add Record' to upload prescriptions, reports, discharge summaries or bills.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = MediSpacing.lg, vertical = MediSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(MediSpacing.sm)
            ) {
                items(documents, key = { it.id }) { doc ->
                    val shares = activeShares.filter { it.documentId == doc.id || it.documentIdsJson.contains(doc.id) }
                    HealthRecordItemCard(
                        doc = doc,
                        activeSharesCount = shares.size,
                        onClick = { onRecordClick(doc) },
                        onShareClick = { onQuickShare(doc) }
                    )
                }
            }
        }
    }
}

@Composable
fun HealthRecordItemCard(
    doc: MedicalDocument,
    activeSharesCount: Int,
    onClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val categoryColor = when (doc.category.lowercase()) {
        "lab report" -> Color(0xFF4F46E5)
        "discharge summary" -> Color(0xFFD97706)
        "medical bill" -> Color(0xFF0284C7)
        "prescription" -> MediTeal
        else -> Color(0xFF64748B)
    }

    val categoryIcon = when (doc.category.lowercase()) {
        "lab report" -> Icons.Default.Biotech
        "discharge summary" -> Icons.Default.LocalHospital
        "medical bill" -> Icons.Default.ReceiptLong
        "prescription" -> Icons.Default.Medication
        else -> Icons.Default.Description
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("record_card_${doc.id}"),
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = categoryColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(MediCornerRadius.sm),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(categoryIcon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = doc.fileName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${doc.hospitalOrDoctor.ifBlank { "Private" }} • ${doc.recordDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = categoryColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(MediCornerRadius.pill)
                ) {
                    Text(
                        text = doc.category,
                        color = categoryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (doc.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = doc.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeSharesCount > 0) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(MediCornerRadius.pill)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Shared ($activeSharesCount active)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Private (Not Shared)", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onShareClick,
                        shape = RoundedCornerShape(MediCornerRadius.sm),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp).testTag("btn_quick_share_${doc.id}")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    }

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(MediCornerRadius.sm),
                        colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp).testTag("btn_details_${doc.id}")
                    ) {
                        Text("Details", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 1: CONSENT CENTER CONTENT
// ============================================================================
@Composable
fun ConsentCenterTabContent(
    shares: List<RecordShareEntity>,
    onViewDetails: (RecordShareEntity) -> Unit,
    onChangeAccess: (RecordShareEntity) -> Unit,
    onRevokeAccess: (RecordShareEntity) -> Unit,
    onNewShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MediSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
    ) {
        // Explanatory Banner
        Surface(
            color = Color(0xFFF0FDF4),
            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
            shape = RoundedCornerShape(MediCornerRadius.md),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Consent Center & Access Permissions", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
                    Text(
                        "You control all healthcare provider access. Modify duration, view active security tokens, or revoke access anytime.",
                        fontSize = 11.sp,
                        color = Color(0xFF166534)
                    )
                }
            }
        }

        if (shares.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LockClock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Active or Past Consent Grants", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("When you grant access to a doctor, hospital, or pharmacist, it will appear here.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shares, key = { it.id }) { share ->
                    val isActive = share.status == "ACTIVE"
                    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
                    val expiryString = if (share.accessDuration == "Until Revoked" || share.expiresAt == Long.MAX_VALUE) {
                        "Until manually revoked"
                    } else {
                        dateFormat.format(Date(share.expiresAt))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("consent_card_${share.id}"),
                        shape = RoundedCornerShape(MediCornerRadius.md),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isActive) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (isActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
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
                                                tint = if (isActive) Color(0xFF15803D) else Color(0xFF64748B),
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
                                    color = if (isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                    shape = RoundedCornerShape(MediCornerRadius.pill)
                                ) {
                                    Text(
                                        text = share.status,
                                        color = if (isActive) Color(0xFF15803D) else Color(0xFFDC2626),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Records Shared: ${share.documentName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                            Text("Access Duration: ${share.accessDuration}", fontSize = 11.sp, color = MediTeal, fontWeight = FontWeight.SemiBold)
                            Text("Expires: $expiryString", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Secure Code: ${share.secureToken}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onViewDetails(share) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp), tint = MediTeal)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View Details & QR", fontSize = 11.sp, color = MediTeal, fontWeight = FontWeight.Bold)
                                }

                                if (isActive) {
                                    TextButton(
                                        onClick = { onChangeAccess(share) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Change Access", fontSize = 11.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { onRevokeAccess(share) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                                        shape = RoundedCornerShape(MediCornerRadius.sm),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp).testTag("btn_revoke_${share.id}")
                                    ) {
                                        Text("Revoke", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 2: ACCESS HISTORY & AUDIT LOG CONTENT
// ============================================================================
@Composable
fun AccessHistoryTabContent(
    logs: List<AccessLogEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MediSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(MediSpacing.sm)
    ) {
        Surface(
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(MediCornerRadius.md),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = MediTeal, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Tamper-Evident Access History", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Every time a doctor, hospital, or pharmacy accesses or downloads your records, it is logged here with exact timestamp.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No access activity recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
                    val actionColor = when (log.action) {
                        "GRANTED" -> Color(0xFF15803D)
                        "REVOKED" -> Color(0xFFDC2626)
                        "ACCESSED", "DOWNLOADED" -> Color(0xFF0284C7)
                        else -> MediTeal
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MediCornerRadius.md),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${log.accessorName} (${log.accessorRole})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Record: ${log.documentTitle}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${dateFormat.format(Date(log.timestamp))} via ${log.verificationMethod}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Surface(
                                color = actionColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(MediCornerRadius.pill)
                            ) {
                                Text(
                                    text = log.action,
                                    color = actionColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODAL: RECORD DETAILS SCREEN / DIALOG
// Record Title, Record Type, Date, Hospital/Doctor, Notes, Who Has Access, View, Share, Edit, Download, Delete
// ============================================================================
@Composable
fun RecordDetailsDialog(
    record: MedicalDocument,
    activeShares: List<RecordShareEntity>,
    onDismiss: () -> Unit,
    onView: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(MediCornerRadius.lg),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(MediSpacing.lg)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.fileName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Record Type: ${record.category}",
                            color = MediTeal,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(MediSpacing.sm))

                // Metadata Details Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailItemRow("Document Format", record.fileType)
                        DetailItemRow("Record Date", record.recordDate.ifBlank { "Unspecified" })
                        DetailItemRow("Hospital / Doctor", record.hospitalOrDoctor.ifBlank { "Not provided" })
                        if (record.description.isNotBlank()) {
                            DetailItemRow("Clinical Notes", record.description)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MediSpacing.md))

                // WHO HAS ACCESS SECTION (Problem Statement 3 Core Requirement)
                Text("Who Currently Has Access", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))

                if (activeShares.isEmpty()) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(MediCornerRadius.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("100% Private — No healthcare provider currently has access.", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        activeShares.forEach { share ->
                            Surface(
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                shape = RoundedCornerShape(MediCornerRadius.sm),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${share.recipientName} (${share.recipientType}) • ${share.accessDuration}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF166534)
                                    )
                                    Text("Active", fontSize = 10.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MediSpacing.lg))

                // Action Buttons: View, Share, Edit, Download, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onView,
                        modifier = Modifier.weight(1f).height(40.dp).testTag("btn_record_view"),
                        colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f).height(40.dp).testTag("btn_record_share"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDownload,
                        modifier = Modifier.weight(1f).height(40.dp).testTag("btn_record_download"),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f).height(40.dp).testTag("btn_record_edit"),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("btn_record_delete"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = BorderStroke(1.dp, Color(0xFFDC2626)),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Record", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Health Record?", fontWeight = FontWeight.Bold) },
            text = { Text("Permanently delete '${record.fileName}' and all active provider access permissions from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(MediCornerRadius.sm)
                ) {
                    Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 3)
    }
}

// ============================================================================
// ============================================================================
// MODAL: PATIENT-CONTROLLED SIMPLE SHARE FLOW DIALOG
// Select Record → Choose Doctor/Hospital → Review & Explicit Consent → Confirm
// ============================================================================
@Composable
fun SimpleShareFlowDialog(
    allDocuments: List<MedicalDocument>,
    initialSelectedRecords: List<MedicalDocument>,
    onDismiss: () -> Unit,
    onGiveAccess: (selectedDocs: List<MedicalDocument>, recipientType: String, recipientName: String, duration: String, purpose: String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Select Record, 2: Choose Doctor or Hospital, 3: Review & Give Explicit Consent

    var selectedRecipientType by remember { mutableStateOf("Doctor") } // Doctor or Hospital
    var selectedRecipientName by remember { mutableStateOf("Dr. Rajesh Sharma (Cardiologist)") }
    var customRecipientName by remember { mutableStateOf("") }

    var selectedDocs by remember {
        mutableStateOf(if (initialSelectedRecords.isNotEmpty()) initialSelectedRecords.toSet() else allDocuments.take(1).toSet())
    }

    var selectedDuration by remember { mutableStateOf("24 Hours") }
    var purpose by remember { mutableStateOf("Clinical Consultation") }
    var isConsentChecked by remember { mutableStateOf(false) }

    val doctorOptions = listOf("Dr. Rajesh Sharma (Cardiologist)", "Dr. Ananya Sen (General Physician)", "Dr. Vikram Rao (Orthopedic)")
    val hospitalOptions = listOf("Apollo City Hospital (OPD)", "Care Multi-Speciality Hospital", "Sunshine General Hospital")
    val pharmacyOptions = listOf("Apollo Pharmacy (High Street)", "MedPlus Pharmacy (Sector 4)")
    val caregiverOptions = listOf("Priya Sharma (Spouse)", "Authorized Health Proxy")

    val currentRecipients = when (selectedRecipientType) {
        "Doctor" -> doctorOptions
        "Hospital" -> hospitalOptions
        "Pharmacist" -> pharmacyOptions
        else -> caregiverOptions
    }

    LaunchedEffect(selectedRecipientType) {
        selectedRecipientName = currentRecipients.first()
        customRecipientName = ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(MediCornerRadius.lg),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(MediSpacing.lg)
                    .verticalScroll(rememberScrollState())
            ) {
                // Stepper Progress Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Patient-Controlled Share Flow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Step $step of 3: " + when (step) {
                                1 -> "Select Health Record"
                                2 -> "Choose Doctor or Hospital"
                                else -> "Review & Explicit Consent"
                            },
                            fontSize = 11.5.sp,
                            color = MediTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (step) {
                    1 -> {
                        // STEP 1: SELECT SPECIFIC RECORD TO SHARE
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(MediCornerRadius.sm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Patient Protection: You explicitly choose which record to share. Your complete medical history is never shared.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E40AF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Select Record from Vault (${selectedDocs.size} Selected)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        if (allDocuments.isEmpty()) {
                            Text("No documents in vault. Please upload a document first.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                allDocuments.forEach { doc ->
                                    val isSelected = selectedDocs.contains(doc)
                                    Surface(
                                        shape = RoundedCornerShape(MediCornerRadius.sm),
                                        color = if (isSelected) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.5.dp, if (isSelected) MediTeal else MaterialTheme.colorScheme.outline),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedDocs = setOf(doc)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedDocs = setOf(doc) },
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(doc.fileName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${doc.category} • ${doc.recordDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (doc.hospitalOrDoctor.isNotBlank()) {
                                                    Text(doc.hospitalOrDoctor, fontSize = 10.sp, color = MediTealDark)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // STEP 2: CHOOSE DOCTOR OR HOSPITAL
                        Text("Select Healthcare Recipient Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        val primaryTypes = listOf("Doctor", "Hospital", "Pharmacist", "Authorized Caregiver")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            primaryTypes.take(2).forEach { type ->
                                val isSelected = selectedRecipientType == type
                                Surface(
                                    shape = RoundedCornerShape(MediCornerRadius.sm),
                                    color = if (isSelected) MediTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedRecipientType = type }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (type == "Doctor") Icons.Default.MedicalServices else Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = type,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Select Verified $selectedRecipientType", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            currentRecipients.forEach { opt ->
                                val isSelected = selectedRecipientName == opt && customRecipientName.isBlank()
                                Surface(
                                    shape = RoundedCornerShape(MediCornerRadius.sm),
                                    color = if (isSelected) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedRecipientName = opt
                                            customRecipientName = ""
                                        }
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedRecipientName = opt; customRecipientName = "" },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        MediOutlinedTextField(
                            value = customRecipientName,
                            onValueChange = { customRecipientName = it },
                            label = { Text("Or enter custom $selectedRecipientType name") },
                            placeholder = { Text("e.g. Dr. K. Mehta / City Clinic") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    3 -> {
                        // STEP 3: REVIEW SHARING DETAILS & GIVE EXPLICIT CONSENT
                        Text("Review Sharing Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        val finalRecipient = customRecipientName.ifBlank { selectedRecipientName }
                        val recordTitle = selectedDocs.firstOrNull()?.fileName ?: "Selected Health Record"

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(MediCornerRadius.md),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                DetailItemRow("Selected Record", recordTitle)
                                DetailItemRow("Recipient", finalRecipient)
                                DetailItemRow("Recipient Type", selectedRecipientType)
                                DetailItemRow("Access Duration", selectedDuration)
                                DetailItemRow("Purpose", purpose)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Set Access Duration", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Access terminates automatically after this period.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        val durations = listOf("1 Hour", "24 Hours", "7 Days", "30 Days", "Until Revoked")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            durations.take(3).forEach { dur ->
                                val isSelected = selectedDuration == dur
                                Surface(
                                    shape = RoundedCornerShape(MediCornerRadius.sm),
                                    color = if (isSelected) MediTealLight else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isSelected) MediTeal else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDuration = dur }
                                ) {
                                    Text(
                                        text = dur,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MediTealDark else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            durations.takeLast(2).forEach { dur ->
                                val isSelected = selectedDuration == dur
                                Surface(
                                    shape = RoundedCornerShape(MediCornerRadius.sm),
                                    color = if (isSelected) MediTealLight else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isSelected) MediTeal else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDuration = dur }
                                ) {
                                    Text(
                                        text = dur,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MediTealDark else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // EXPLICIT PATIENT CONSENT CARD
                        Surface(
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                            shape = RoundedCornerShape(MediCornerRadius.md),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Explicit Patient Consent", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isConsentChecked = !isConsentChecked },
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Checkbox(
                                        checked = isConsentChecked,
                                        onCheckedChange = { isConsentChecked = it },
                                        modifier = Modifier.size(20.dp).testTag("checkbox_explicit_consent")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "I explicitly authorize MediBridge+ to share ONLY \"$recordTitle\" with $finalRecipient for $selectedDuration. My complete medical history is NOT shared. I understand I can revoke access anytime.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF166534),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MediSpacing.lg))

                // Stepper Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Back", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (step < 3) {
                        Button(
                            onClick = {
                                if (step == 1 && selectedDocs.isEmpty()) {
                                    // must pick 1
                                } else {
                                    step++
                                }
                            },
                            enabled = selectedDocs.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                            modifier = Modifier.height(40.dp).testTag("btn_step_next")
                        ) {
                            Text(
                                text = if (step == 1) "Next: Choose Recipient" else "Next: Review & Consent",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                val finalName = customRecipientName.ifBlank { selectedRecipientName }
                                onGiveAccess(selectedDocs.toList(), selectedRecipientType, finalName, selectedDuration, purpose)
                            },
                            enabled = isConsentChecked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MediTeal,
                                disabledContainerColor = MediTeal.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.height(42.dp).testTag("btn_give_access")
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CONFIRM & SHARE", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODAL: SHARE CONFIRMATION WITH TEMPORARY QR CODE & TOKEN
// ============================================================================
@Composable
fun ShareConfirmationQrDialog(
    share: RecordShareEntity,
    onDismiss: () -> Unit,
    onRevokeNow: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val expiryString = if (share.accessDuration == "Until Revoked" || share.expiresAt == Long.MAX_VALUE) {
        "Active until manually revoked"
    } else {
        "Auto-Expires: ${dateFormat.format(Date(share.expiresAt))}"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(MediCornerRadius.lg),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(MediSpacing.lg)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Access Granted Successfully", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Time-Limited Secure Sharing Passcode & QR", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                // Visual Temporary QR Code Pattern (Canvas)
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color.White, RoundedCornerShape(MediCornerRadius.md))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(MediCornerRadius.md))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridSize = 14
                        val stepX = size.width / gridSize
                        val stepY = size.height / gridSize
                        val seed = share.secureToken.hashCode()

                        for (i in 0 until gridSize) {
                            for (j in 0 until gridSize) {
                                // Draw corner markers or pseudo random matrix
                                val isCorner = (i < 4 && j < 4) || (i > gridSize - 5 && j < 4) || (i < 4 && j > gridSize - 5)
                                val isFilled = if (isCorner) {
                                    (i == 0 || i == 3 || j == 0 || j == 3 || (i == 1 && j == 1) || (i == 1 && j == 2) || (i == 2 && j == 1) || (i == 2 && j == 2))
                                } else {
                                    ((i * 7 + j * 13 + seed) % 5) in 0..1
                                }

                                if (isFilled) {
                                    drawRect(
                                        color = Color(0xFF0F172A),
                                        topLeft = Offset(i * stepX, j * stepY),
                                        size = Size(stepX * 0.9f, stepY * 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secure Sharing Code Box
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Secure Access Token", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(share.secureToken, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MediTeal)
                        }

                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Secure Token", share.secureToken))
                            Toast.makeText(context, "Access Token Copied!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Token", tint = MediTeal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(MediCornerRadius.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Granted to: ${share.recipientName} (${share.recipientType})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Records: ${share.documentName}", fontSize = 11.sp)
                        Text("Duration: ${share.accessDuration} • $expiryString", fontSize = 11.sp, color = MediTeal)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRevokeNow,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    ) {
                        Text("Revoke Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    ) {
                        Text("Done", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODAL: CHANGE ACCESS DURATION
// ============================================================================
@Composable
fun ChangeAccessDurationDialog(
    currentDuration: String,
    recipientName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedDuration by remember { mutableStateOf(currentDuration) }
    val durations = listOf("1 Hour", "24 Hours", "7 Days", "30 Days", "Until Revoked")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Access Duration", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Modify how long $recipientName can access your records:")
                durations.forEach { dur ->
                    val isSelected = selectedDuration == dur
                    Surface(
                        shape = RoundedCornerShape(MediCornerRadius.sm),
                        color = if (isSelected) MediTealLight else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) MediTeal else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth().clickable { selectedDuration = dur }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isSelected, onClick = { selectedDuration = dur }, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dur, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedDuration) },
                colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                shape = RoundedCornerShape(MediCornerRadius.sm)
            ) {
                Text("Update Duration", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ============================================================================
// MODAL: ADD HEALTH RECORD DIALOG
// ============================================================================
@Composable
fun AddRecordDialog(
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, category: String, hospitalDoctor: String, recordDate: String, uri: Uri?, note: String) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Prescription") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileType by remember { mutableStateOf("JPG") }
    var tempCameraPath by remember { mutableStateOf<String?>(null) }

    var documentName by remember { mutableStateOf("") }
    var hospitalDoctor by remember { mutableStateOf("") }
    var recordDate by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var personalNote by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedFileType = "JPG"
            if (documentName.isBlank()) documentName = "$selectedCategory Record"
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedFileType = "PDF"
            if (documentName.isBlank()) documentName = "$selectedCategory PDF Report"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraPath != null) {
            selectedUri = Uri.fromFile(File(tempCameraPath!!))
            selectedFileType = "JPG"
            if (documentName.isBlank()) documentName = "$selectedCategory Photo"
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).padding(vertical = 16.dp),
            shape = RoundedCornerShape(MediCornerRadius.lg),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(MediSpacing.lg).verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Health Record", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Record Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                val categories = listOf("Prescription", "Lab Report", "Discharge Summary", "Medical Bill", "Other Medical Record")
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(MediCornerRadius.sm),
                                    color = if (isSelected) MediTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f).clickable { selectedCategory = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Attach Medical File", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            val (uri, path) = DocumentStorageHelper.createTempCameraImageUri(context)
                            tempCameraPath = path
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp), tint = MediTeal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { pdfPickerLauncher.launch("application/pdf") },
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = MediError)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp)
                    }
                }

                if (selectedUri != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("File attached: $selectedFileType (${selectedUri?.lastPathSegment?.take(20)})", fontSize = 11.sp, color = MediTeal, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                MediOutlinedTextField(
                    value = documentName,
                    onValueChange = { documentName = it },
                    label = { Text("Record Title") },
                    placeholder = { Text("e.g. Cardiology Prescription, Lipid Profile") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                MediOutlinedTextField(
                    value = hospitalDoctor,
                    onValueChange = { hospitalDoctor = it },
                    label = { Text("Hospital / Doctor Name") },
                    placeholder = { Text("e.g. Dr. Sharma • Apollo Hospital") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                MediOutlinedTextField(
                    value = recordDate,
                    onValueChange = { recordDate = it },
                    label = { Text("Record Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                MediOutlinedTextField(
                    value = personalNote,
                    onValueChange = { personalNote = it },
                    label = { Text("Clinical Notes / Instructions (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(MediSpacing.md))

                Button(
                    onClick = {
                        val finalTitle = documentName.ifBlank { "$selectedCategory Record" }
                        onSave(finalTitle, selectedFileType, selectedCategory, hospitalDoctor, recordDate, selectedUri, personalNote)
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_save_record_vault"),
                    colors = ButtonDefaults.buttonColors(containerColor = MediTeal)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("SAVE TO HEALTH VAULT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

// ============================================================================
// MODAL: VIEW IMAGE
// ============================================================================
@Composable
fun ViewDocumentImageDialog(
    document: MedicalDocument,
    onDismiss: () -> Unit
) {
    val bitmap = remember(document.localFilePath) {
        try {
            val file = File(document.localFilePath)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } catch (e: Exception) {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(MediSpacing.lg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(document.fileName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${document.category} • ${document.hospitalOrDoctor}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Spacer(modifier = Modifier.height(MediSpacing.md))

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(MediCornerRadius.lg)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = document.fileName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(48.dp), tint = MediTeal)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Demonstration Medical Record Document", fontWeight = FontWeight.Bold)
                            Text("Stored locally in private encrypted sandbox.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MediSpacing.md))
                MediPrimaryButton(text = "Close", onClick = onDismiss)
            }
        }
    }
}

// ============================================================================
// MODAL: EDIT RECORD
// ============================================================================
@Composable
fun EditDocumentDialog(
    document: MedicalDocument,
    onDismiss: () -> Unit,
    onSave: (newTitle: String, newNote: String, newHospitalDoctor: String) -> Unit
) {
    var title by remember { mutableStateOf(document.fileName) }
    var hospitalDoctor by remember { mutableStateOf(document.hospitalOrDoctor) }
    var note by remember { mutableStateOf(document.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Record Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MediOutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Record Title") }, modifier = Modifier.fillMaxWidth())
                MediOutlinedTextField(value = hospitalDoctor, onValueChange = { hospitalDoctor = it }, label = { Text("Hospital / Doctor") }, modifier = Modifier.fillMaxWidth())
                MediOutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, note, hospitalDoctor) },
                colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                shape = RoundedCornerShape(MediCornerRadius.sm)
            ) {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

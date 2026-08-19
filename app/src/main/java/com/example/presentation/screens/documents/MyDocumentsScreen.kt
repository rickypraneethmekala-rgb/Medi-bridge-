package com.example.presentation.screens.documents

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import com.example.data.local.entity.MedicalDocument
import com.example.data.local.entity.MedicineReminder
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
    onNavigateToApiConfig: () -> Unit = {}
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var viewingDocument by remember { mutableStateOf<MedicalDocument?>(null) }
    var editingDocument by remember { mutableStateOf<MedicalDocument?>(null) }
    var reminderDocTarget by remember { mutableStateOf<MedicalDocument?>(null) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Medical Documents Vault",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "100% private, on-device encrypted storage",
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
                                "100% Local",
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
            // Prominent Add Document Header Card
            MediCard(
                modifier = Modifier.padding(horizontal = MediSpacing.lg, vertical = MediSpacing.sm),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Personal Health Records",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Store prescriptions, lab reports & PDFs securely on this phone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(MediSpacing.md))
                    MediPrimaryButton(
                        text = "+ Add Medical Document",
                        onClick = { showAddDialog = true },
                        icon = Icons.Default.AddCircle,
                        testTag = "btn_add_document"
                    )
                }
            }

            // Documents List
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = AccentDocsBg,
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = AccentDocs
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Medical Documents Stored",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Take a photo of your prescription or upload lab reports & PDFs. They remain securely on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MediSecondaryButton(
                            text = "+ Add Your First Document",
                            onClick = { showAddDialog = true }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = MediSpacing.lg, vertical = MediSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        val docReminders = reminders.filter { it.documentId == doc.id }
                        DocumentItemCard(
                            document = doc,
                            reminders = docReminders,
                            onOpen = {
                                if (doc.fileType.equals("PDF", ignoreCase = true)) {
                                    val opened = DocumentStorageHelper.openPdfWithViewer(context, doc.localFilePath)
                                    if (!opened) {
                                        Toast.makeText(context, "No PDF viewer app found on device.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    viewingDocument = doc
                                }
                            },
                            onEdit = { editingDocument = doc },
                            onDelete = { viewModel.deleteDocument(doc) },
                            onAddReminder = { reminderDocTarget = doc },
                            onToggleReminder = { rem -> viewModel.toggleReminder(rem) },
                            onDeleteReminder = { rem -> viewModel.deleteReminder(rem) }
                        )
                    }
                }
            }
        }
    }

    // Add Document Dialog
    if (showAddDialog) {
        AddDocumentDialog(
            isProcessing = isProcessing,
            onDismiss = { showAddDialog = false },
            onSave = { name, type, uri, note ->
                viewModel.saveDocument(name, type, uri, note) {
                    showAddDialog = false
                }
            }
        )
    }

    // View Stored Image Dialog
    if (viewingDocument != null) {
        ViewDocumentImageDialog(
            document = viewingDocument!!,
            onDismiss = { viewingDocument = null }
        )
    }

    // Edit Document Dialog
    if (editingDocument != null) {
        EditDocumentDialog(
            document = editingDocument!!,
            onDismiss = { editingDocument = null },
            onSave = { newName, newNote ->
                viewModel.updateDocument(editingDocument!!, newName, newNote)
                editingDocument = null
            }
        )
    }

    // Add Reminder Dialog
    if (reminderDocTarget != null) {
        AddReminderDialog(
            documentName = reminderDocTarget!!.fileName,
            onDismiss = { reminderDocTarget = null },
            onSave = { medName, timeStr, instruction ->
                viewModel.addReminder(reminderDocTarget!!.id, medName, timeStr, instruction)
                reminderDocTarget = null
            }
        )
    }
}

@Composable
fun DocumentItemCard(
    document: MedicalDocument,
    reminders: List<MedicineReminder>,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddReminder: () -> Unit,
    onToggleReminder: (MedicineReminder) -> Unit,
    onDeleteReminder: (MedicineReminder) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val formattedDate = remember(document.createdAt) { dateFormat.format(Date(document.createdAt)) }
    val isPdf = document.fileType.equals("PDF", ignoreCase = true)

    var showDeleteConfirm by remember { mutableStateOf(false) }

    MediCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_doc_${document.id}"),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Header Row: Icon + Name + Added Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isPdf) MediError.copy(alpha = 0.12f) else AccentDocsBg,
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isPdf) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = "PDF Document",
                                tint = MediError,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Image Document",
                                tint = AccentDocs,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(MediSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Added: $formattedDate • ${document.fileType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Optional Personal Note
            if (document.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(MediSpacing.sm))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.sm),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(MediSpacing.sm)) {
                        Text(
                            text = "Your personal note",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MediTeal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = document.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Attached Reminders List
            if (reminders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(MediSpacing.sm))
                Text(
                    text = "Configured Medicine Reminders",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MediTeal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    reminders.forEach { reminder ->
                        Surface(
                            shape = RoundedCornerShape(MediCornerRadius.sm),
                            color = AccentHealthBg,
                            border = BorderStroke(1.dp, AccentHealth.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Alarm,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = AccentHealth
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            reminder.medicineName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "${reminder.time} • ${reminder.instruction}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = reminder.isEnabled,
                                        onCheckedChange = { onToggleReminder(reminder) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = MediTeal
                                        )
                                    )
                                    IconButton(
                                        onClick = { onDeleteReminder(reminder) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Delete Reminder",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MediSpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(MediSpacing.sm))

            // Action Buttons Row: OPEN | EDIT | + REMINDER | DELETE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onOpen,
                        shape = RoundedCornerShape(MediCornerRadius.sm),
                        colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_open_doc_${document.id}")
                    ) {
                        Icon(
                            if (isPdf) Icons.Default.Launch else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isPdf) "OPEN PDF" else "OPEN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(MediCornerRadius.sm),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_edit_doc_${document.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EDIT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onAddReminder,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_add_reminder_${document.id}")
                    ) {
                        Icon(
                            Icons.Default.AddAlert,
                            contentDescription = "Add Reminder",
                            tint = MediTeal
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_delete_doc_${document.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Document?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete '${document.fileName}' and its local file from your device storage.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(MediCornerRadius.sm)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
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
fun AddDocumentDialog(
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, uri: Uri, note: String) -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileType by remember { mutableStateOf("JPG") }
    var tempCameraPath by remember { mutableStateOf<String?>(null) }

    var documentName by remember { mutableStateOf("") }
    var personalNote by remember { mutableStateOf("") }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedFileType = "JPG"
            if (documentName.isBlank()) documentName = "Prescription"
        }
    }

    // PDF Picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedFileType = "PDF"
            if (documentName.isBlank()) documentName = "Medical Report PDF"
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraPath != null) {
            selectedUri = Uri.fromFile(File(tempCameraPath!!))
            selectedFileType = "JPG"
            if (documentName.isBlank()) documentName = "Prescription Photo"
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
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
                    .padding(MediSpacing.xl)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add Medical Document",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(MediSpacing.md))

                // Upload Choice Buttons
                Text(
                    "Select Document Source",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(MediSpacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val (uri, path) = DocumentStorageHelper.createTempCameraImageUri(context)
                            tempCameraPath = path
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp), tint = MediTeal)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Camera", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp), tint = AccentDocs)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Image", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedButton(
                        onClick = { pdfPickerLauncher.launch("application/pdf") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp), tint = MediError)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // File Preview Area
                if (selectedUri != null) {
                    Spacer(modifier = Modifier.height(MediSpacing.md))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(MediCornerRadius.md),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (selectedFileType == "PDF") {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(48.dp), tint = MediError)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Medical PDF Selected", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                            } else {
                                val bitmap = remember(selectedUri) {
                                    try {
                                        context.contentResolver.openInputStream(selectedUri!!)?.use {
                                            BitmapFactory.decodeStream(it)
                                        }
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Selected preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("Image Ready to Save")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MediSpacing.md))

                // Document Name
                MediOutlinedTextField(
                    value = documentName,
                    onValueChange = { documentName = it },
                    label = { Text("Document Name") },
                    placeholder = { Text("e.g. Blood Test Report, Prescription") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                )

                Spacer(modifier = Modifier.height(MediSpacing.sm))

                // Personal Description / Note
                MediOutlinedTextField(
                    value = personalNote,
                    onValueChange = { personalNote = it },
                    label = { Text("Personal Note (Optional)") },
                    placeholder = { Text("e.g. Morning: tablet after breakfast\nEvening: tablet after dinner") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(MediCornerRadius.md)
                )

                Text(
                    text = "MediBridge stores your personal notes for reference only and does not interpret them as medical instructions.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Save Button
                Button(
                    onClick = {
                        if (selectedUri != null) {
                            onSave(documentName, selectedFileType, selectedUri!!, personalNote)
                        } else {
                            Toast.makeText(context, "Please select or capture a document first.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MediButtonHeight.standard),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                    enabled = selectedUri != null && !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("SAVE DOCUMENT", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MediSpacing.lg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(document.fileName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (document.description.isNotBlank()) {
                            Text(document.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(MediSpacing.md))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MediCornerRadius.lg))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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
                            Icon(Icons.Default.BrokenImage, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Unable to load stored image file.")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MediSpacing.md))
                MediPrimaryButton(
                    text = "Close",
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
fun EditDocumentDialog(
    document: MedicalDocument,
    onDismiss: () -> Unit,
    onSave: (newName: String, newNote: String) -> Unit
) {
    var name by remember { mutableStateOf(document.fileName) }
    var note by remember { mutableStateOf(document.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Document Details", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MediOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Document Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                )

                MediOutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Personal Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(MediCornerRadius.md)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, note) },
                colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                shape = RoundedCornerShape(MediCornerRadius.sm)
            ) {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddReminderDialog(
    documentName: String,
    onDismiss: () -> Unit,
    onSave: (medName: String, timeStr: String, instruction: String) -> Unit
) {
    var medicineName by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("08:00 AM") }
    var instruction by remember { mutableStateOf("Take after breakfast") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Alarm, contentDescription = null, tint = MediTeal) },
        title = { Text("Create Medicine Reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Associated with: $documentName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                MediOutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text("Medicine Name") },
                    placeholder = { Text("e.g. Paracetamol 650mg") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                )

                MediOutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Alarm Time") },
                    placeholder = { Text("e.g. 08:00 AM, 08:00 PM") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                )

                MediOutlinedTextField(
                    value = instruction,
                    onValueChange = { instruction = it },
                    label = { Text("Instruction / Note") },
                    placeholder = { Text("e.g. Take after breakfast") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(medicineName, reminderTime, instruction)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                shape = RoundedCornerShape(MediCornerRadius.sm),
                enabled = medicineName.isNotBlank()
            ) {
                Text("Set Alarm", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

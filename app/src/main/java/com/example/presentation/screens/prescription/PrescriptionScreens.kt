package com.example.presentation.screens.prescription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.network.ApiResult
import com.example.data.remote.dto.PrescriptionDto
import com.example.data.remote.dto.PrescriptionMedicineDto
import com.example.presentation.common.*
import com.example.presentation.viewmodel.PrescriptionAiViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionAiScreen(
    viewModel: PrescriptionAiViewModel,
    onNavigateToApiConfig: () -> Unit
) {
    val prescriptionsState by viewModel.prescriptionsState.collectAsState()
    val ocrState by viewModel.ocrState.collectAsState()
    val aiExplanationState by viewModel.aiExplanationState.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    var showUploadDialog by remember { mutableStateOf(false) }
    var userQueryMedicine by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Health Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Safe multilingual dosage and medicine guidance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showUploadDialog = true },
                        modifier = Modifier.testTag("btn_upload_rx_ocr")
                    ) {
                        Icon(
                            Icons.Default.DocumentScanner,
                            contentDescription = "Scan Prescription OCR",
                            tint = AccentAi
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(MediSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
        ) {
            // Mandatory Medical Disclaimer
            item {
                MedicalDisclaimerBanner()
            }

            // Language Selector for AI Health Explanation & Multilingual Voice
            item {
                MediCard(
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = AccentAi
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Language:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = selectedLanguage == "en",
                                onClick = { viewModel.setLanguage("en") },
                                label = { Text("English", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentAiBg,
                                    selectedLabelColor = AccentAi
                                )
                            )
                            FilterChip(
                                selected = selectedLanguage == "te",
                                onClick = { viewModel.setLanguage("te") },
                                label = { Text("తెలుగు", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentAiBg,
                                    selectedLabelColor = AccentAi
                                )
                            )
                            FilterChip(
                                selected = selectedLanguage == "hi",
                                onClick = { viewModel.setLanguage("hi") },
                                label = { Text("हिन्दी", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentAiBg,
                                    selectedLabelColor = AccentAi
                                )
                            )
                        }
                    }
                }
            }

            // Quick AI Question / Search Bar
            item {
                MediCard(
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(MediCornerRadius.sm))
                                    .background(AccentAiBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = AccentAi,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(MediSpacing.sm))
                            Text(
                                "Ask AI Health Guide",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Get verified dosage precautions and purpose in your chosen language.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(MediSpacing.md))

                        MediOutlinedTextField(
                            value = userQueryMedicine,
                            onValueChange = { userQueryMedicine = it },
                            placeholder = { Text("Enter medicine (e.g. Paracetamol 500mg)...") },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (userQueryMedicine.isNotBlank()) {
                                            viewModel.requestAiExplanation(userQueryMedicine, "Standard Prescribed Dosage")
                                        }
                                    },
                                    modifier = Modifier.testTag("btn_submit_ai_query")
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Ask AI",
                                        tint = AccentAi
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(MediCornerRadius.md),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(MediSpacing.sm))

                        // Quick suggestion chips
                        Text(
                            "Suggested:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("Paracetamol 500mg", "Amoxicillin 250mg", "Metformin 500mg", "ORS Sachet", "Cetirizine 10mg")) { sampleMed ->
                                SuggestionChip(
                                    onClick = {
                                        userQueryMedicine = sampleMed
                                        viewModel.requestAiExplanation(sampleMed, "Standard Prescribed Dosage")
                                    },
                                    label = { Text(sampleMed, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(MediCornerRadius.pill)
                                )
                            }
                        }
                    }
                }
            }

            // AI Explanation Output Result Card if available
            if (aiExplanationState != null) {
                item {
                    when (val exp = aiExplanationState) {
                        is ApiResult.Loading -> {
                            MediCard(
                                backgroundColor = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = AccentAi
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Generating clinical explanation...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        is ApiResult.Unconfigured -> {
                            ApiUnavailableCard(
                                serviceName = exp.serviceName,
                                endpointName = exp.requiredEndpoint,
                                onConfigureClick = onNavigateToApiConfig
                            )
                        }
                        is ApiResult.Success -> {
                            val data = exp.data
                            MediCard(
                                modifier = Modifier.testTag("card_ai_explanation_result"),
                                backgroundColor = AccentAiBg,
                                borderColor = AccentAi.copy(alpha = 0.3f)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = AccentAi,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "AI Guide: ${data.medicineName}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        IconButton(onClick = { viewModel.clearExplanationState() }) {
                                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Purpose: ${data.generalPurpose}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Dosage & Frequency: ${data.doctorPrescribedDosage} (${data.frequency})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Precautions: ${data.precautions}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(MediSpacing.md))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.playAudioExplanation(data.audioExplanationText) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentAi),
                                            shape = RoundedCornerShape(MediCornerRadius.sm),
                                            modifier = Modifier.testTag("btn_listen_tts")
                                        ) {
                                            Icon(
                                                Icons.Default.VolumeUp,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Listen (${data.language.uppercase()})", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        if (isSpeaking) {
                                            OutlinedButton(
                                                onClick = { viewModel.stopAudio() },
                                                shape = RoundedCornerShape(MediCornerRadius.sm)
                                            ) {
                                                Text("Stop Voice")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            // Prescription Records Section
            item {
                MediSectionHeader(
                    title = "Doctor Prescriptions",
                    accentColor = AccentAi,
                    actionText = "Scan OCR",
                    onActionClick = { showUploadDialog = true }
                )
            }

            when (val state = prescriptionsState) {
                is ApiResult.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentAi)
                        }
                    }
                }
                is ApiResult.Unconfigured -> {
                    item {
                        ApiUnavailableCard(
                            serviceName = state.serviceName,
                            endpointName = state.requiredEndpoint,
                            onConfigureClick = onNavigateToApiConfig
                        )
                    }
                }
                is ApiResult.Success -> {
                    val list = state.data
                    if (list.isEmpty()) {
                        item {
                            MediCard(
                                backgroundColor = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(MediSpacing.md),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "No digital prescriptions synced yet.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(list) { rx ->
                            PrescriptionCardItem(
                                prescription = rx,
                                onExplainMedicine = { med ->
                                    viewModel.requestAiExplanation(med.medicineName, med.dosage)
                                }
                            )
                        }
                    }
                }
                else -> {}
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Prescription OCR Extraction", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Scan or upload your doctor's prescription to extract medicine names and schedule for AI guidance.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "MediBridge uses AI strictly for patient health education and understanding. Always follow your physician's exact instructions.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUploadDialog = false
                        viewModel.processPrescriptionOcr("https://medibridge.org/uploads/rx_sample.jpg")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAi),
                    shape = RoundedCornerShape(MediCornerRadius.sm)
                ) {
                    Text("Process Scan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PrescriptionCardItem(
    prescription: PrescriptionDto,
    onExplainMedicine: (PrescriptionMedicineDto) -> Unit
) {
    MediCard(
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    "Dr. ${prescription.doctorName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    prescription.hospitalName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MediStatusBadge(
                statusText = if (prescription.isVerifiedByPharmacist) "Pharmacist Verified" else "Pending Verification",
                statusType = if (prescription.isVerifiedByPharmacist) MediStatusType.SUCCESS else MediStatusType.WARNING
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Issued Date: ${prescription.issuedDate}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(MediSpacing.md))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(MediSpacing.sm))

        Text(
            "Prescribed Medicines:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        prescription.medicines.forEach { med ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "• ${med.medicineName} (${med.dosage})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${med.frequency} for ${med.durationDays} days",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = { onExplainMedicine(med) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AccentAiBg,
                        contentColor = AccentAi
                    ),
                    shape = RoundedCornerShape(MediCornerRadius.pill)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AccentAi
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Explain", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

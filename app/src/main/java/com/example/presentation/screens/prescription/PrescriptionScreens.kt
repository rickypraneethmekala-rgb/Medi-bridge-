package com.example.presentation.screens.prescription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.core.network.ApiResult
import com.example.data.remote.dto.PrescriptionDto
import com.example.data.remote.dto.PrescriptionMedicineDto
import com.example.presentation.common.ApiUnavailableCard
import com.example.presentation.common.MedicalDisclaimerBanner
import com.example.presentation.viewmodel.PrescriptionAiViewModel

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prescriptions & AI OCR", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload Rx")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mandatory Medical Disclaimer
            MedicalDisclaimerBanner()

            // Language Selector for AI Explanation & TTS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("AI Voice Language:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedLanguage == "en",
                        onClick = { viewModel.setLanguage("en") },
                        label = { Text("English") }
                    )
                    FilterChip(
                        selected = selectedLanguage == "te",
                        onClick = { viewModel.setLanguage("te") },
                        label = { Text("తెలుగు") }
                    )
                    FilterChip(
                        selected = selectedLanguage == "hi",
                        onClick = { viewModel.setLanguage("hi") },
                        label = { Text("हिन्दी") }
                    )
                }
            }

            // AI Explanation Bottom Card if triggered
            if (aiExplanationState != null) {
                when (val exp = aiExplanationState) {
                    is ApiResult.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("AI Medicine Guide: ${data.medicineName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    IconButton(onClick = { viewModel.clearExplanationState() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close")
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Purpose: ${data.generalPurpose}", style = MaterialTheme.typography.bodySmall)
                                Text("Dosage: ${data.doctorPrescribedDosage} (${data.frequency})", style = MaterialTheme.typography.bodySmall)
                                Text("Precautions: ${data.precautions}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.playAudioExplanation(data.audioExplanationText) },
                                        modifier = Modifier.testTag("btn_listen_tts")
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Listen (${data.language.uppercase()})")
                                    }
                                    if (isSpeaking) {
                                        OutlinedButton(onClick = { viewModel.stopAudio() }) {
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

            // Prescription List
            when (val state = prescriptionsState) {
                is ApiResult.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ApiResult.Unconfigured -> {
                    ApiUnavailableCard(
                        serviceName = state.serviceName,
                        endpointName = state.requiredEndpoint,
                        onConfigureClick = onNavigateToApiConfig
                    )
                }
                is ApiResult.Success -> {
                    val list = state.data
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No prescriptions found. Upload or receive from doctor.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
                }
                else -> {}
            }
        }
    }

    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Upload Prescription Image") },
            text = {
                Column {
                    Text("Select or snap your prescription image to send to the configured OCR API.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "If OCR confidence is low, MediBridge requires pharmacist verification prior to ordering.",
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
                    }
                ) {
                    Text("Process OCR")
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Dr. ${prescription.doctorName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(prescription.hospitalName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = if (prescription.isVerifiedByPharmacist) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (prescription.isVerifiedByPharmacist) "Pharmacist Verified" else "Pending Verification",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Date: ${prescription.issuedDate}", style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(10.dp))
            Text("Prescribed Medicines:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            prescription.medicines.forEach { med ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("• ${med.medicineName} (${med.dosage})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${med.frequency} for ${med.durationDays} days", style = MaterialTheme.typography.labelSmall)
                    }
                    FilledTonalButton(
                        onClick = { onExplainMedicine(med) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Explain", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

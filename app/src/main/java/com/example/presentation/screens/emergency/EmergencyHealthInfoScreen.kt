package com.example.presentation.screens.emergency

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.EmergencyHealthInfoEntity
import com.example.presentation.common.*
import com.example.presentation.viewmodel.MyDocumentsViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyHealthInfoScreen(
    documentsViewModel: MyDocumentsViewModel,
    onTriggerSOS: () -> Unit = {}
) {
    val context = LocalContext.current
    val emergencyInfo by documentsViewModel.emergencyInfo.collectAsState()
    val info = emergencyInfo ?: EmergencyHealthInfoEntity()

    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Emergency Health Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Critical health data & patient-controlled emergency access",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.testTag("btn_edit_emergency_info")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Emergency Info", tint = MediTeal)
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
                .testTag("screen_emergency_health_info"),
            contentPadding = PaddingValues(MediSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MediSpacing.lg)
        ) {
            // SOS Emergency Ambulance Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.5.dp, Color(0xFFFCA5A5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color(0xFFDC2626),
                            shape = CircleShape,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Emergency, contentDescription = "SOS", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Emergency Medical Assistance",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            "Dial 108 (Ambulance) or 112 (National Emergency)",
                            fontSize = 12.sp,
                            color = Color(0xFFB91C1C)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(MediCornerRadius.md),
                                modifier = Modifier.weight(1f).testTag("btn_call_108")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call 108", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = onTriggerSOS,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                                shape = RoundedCornerShape(MediCornerRadius.md),
                                modifier = Modifier.weight(1f).testTag("btn_trigger_sos_modal")
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SOS Broadcast", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Patient-Controlled Emergency Sharing Switch
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    colors = CardDefaults.cardColors(
                        containerColor = if (info.isSharingAllowedInEmergency) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (info.isSharingAllowedInEmergency) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                color = if (info.isSharingAllowedInEmergency) Color(0xFFDCFCE7) else MediTealLight,
                                shape = CircleShape,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (info.isSharingAllowedInEmergency) Color(0xFF16A34A) else MediTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Emergency Responder Access",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (info.isSharingAllowedInEmergency) {
                                        "Active: Paramedics can view Blood Group, Allergies & Emergency Contact"
                                    } else {
                                        "Disabled: Emergency responders cannot access your emergency card"
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = info.isSharingAllowedInEmergency,
                            onCheckedChange = { allowed ->
                                documentsViewModel.toggleEmergencySharing(allowed)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MediTeal),
                            modifier = Modifier.testTag("switch_emergency_sharing")
                        )
                    }
                }
            }

            // Blood Group & Allergies Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MediSpacing.md)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(MediCornerRadius.md),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bloodtype, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Blood Group", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = info.bloodGroup.ifBlank { "Not Specified" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(MediCornerRadius.md),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Known Allergies", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = info.allergies.ifBlank { "None Reported" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Current Medicines
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Medication, contentDescription = null, tint = MediTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Current Chronic Medicines", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = info.currentMedicines.ifBlank { "No active chronic medications recorded." },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (info.chronicConditions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Conditions: ${info.chronicConditions}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Emergency Contact Person
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContactPhone, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Primary Emergency Contact", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Surface(
                                color = Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(MediCornerRadius.pill)
                            ) {
                                Text(
                                    "Next of Kin",
                                    color = Color(0xFF0369A1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = info.emergencyContactName.ifBlank { "Not Specified" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = info.emergencyContactPhone.ifBlank { "No phone number added" },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (info.emergencyContactPhone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${info.emergencyContactPhone}"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                                    shape = RoundedCornerShape(MediCornerRadius.sm),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Edit Action Button
            item {
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    border = BorderStroke(1.dp, MediTeal)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MediTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Update Emergency Health Information", fontWeight = FontWeight.Bold, color = MediTeal)
                }
            }
        }
    }

    // Edit Emergency Dialog
    if (showEditDialog) {
        var bloodGroup by remember { mutableStateOf(info.bloodGroup) }
        var allergies by remember { mutableStateOf(info.allergies) }
        var chronic by remember { mutableStateOf(info.chronicConditions) }
        var meds by remember { mutableStateOf(info.currentMedicines) }
        var contactName by remember { mutableStateOf(info.emergencyContactName) }
        var contactPhone by remember { mutableStateOf(info.emergencyContactPhone) }
        var shareEmergency by remember { mutableStateOf(info.isSharingAllowedInEmergency) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Emergency Health Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediOutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("Blood Group (e.g. O+, B+, AB-)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    MediOutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Known Allergies (e.g. Penicillin, Sulfa)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    MediOutlinedTextField(
                        value = chronic,
                        onValueChange = { chronic = it },
                        label = { Text("Chronic Conditions (e.g. Hypertension)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    MediOutlinedTextField(
                        value = meds,
                        onValueChange = { meds = it },
                        label = { Text("Current Daily Medicines") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    MediOutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Emergency Contact Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    MediOutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Emergency Contact Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = shareEmergency, onCheckedChange = { shareEmergency = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Allow Emergency Responder Access", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        documentsViewModel.updateEmergencyHealthInfo(
                            EmergencyHealthInfoEntity(
                                bloodGroup = bloodGroup.trim(),
                                allergies = allergies.trim(),
                                chronicConditions = chronic.trim(),
                                currentMedicines = meds.trim(),
                                emergencyContactName = contactName.trim(),
                                emergencyContactPhone = contactPhone.trim(),
                                isSharingAllowedInEmergency = shareEmergency
                            )
                        )
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MediTeal)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

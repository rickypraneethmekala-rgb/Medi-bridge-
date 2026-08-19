package com.example.presentation.screens.health

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
import com.example.data.local.entity.LocalFamilyMemberEntity
import com.example.data.local.entity.LocalReminderEntity
import com.example.presentation.common.MedicalDisclaimerBanner
import com.example.presentation.viewmodel.ReminderFamilyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: ReminderFamilyViewModel,
    onNavigateToApiConfig: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val isSeniorMode by viewModel.isSeniorMode.collectAsState()

    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showAddFamilyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health & Reminders", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MedicalDisclaimerBanner()
            }

            // Senior Citizen Accessibility Mode Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Senior Citizen Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Large text, simplified touch navigation & voice assistance", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = isSeniorMode,
                            onCheckedChange = { viewModel.toggleSeniorMode(it) },
                            modifier = Modifier.testTag("switch_senior_mode")
                        )
                    }
                }
            }

            // Medicine Reminders Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MEDICINE REMINDERS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { showAddReminderDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Reminder", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (reminders.isEmpty()) {
                item {
                    Text("No medicine reminders created yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(reminders) { reminder ->
                    ReminderItemCard(
                        reminder = reminder,
                        onTaken = { viewModel.markDoseTaken(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder) }
                    )
                }
            }

            // Family Health Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FAMILY HEALTH MANAGERS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { showAddFamilyDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Member", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (familyMembers.isEmpty()) {
                item {
                    Text("No family members added. Add parents/dependents to manage their appointments and care.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(familyMembers) { member ->
                    FamilyMemberCard(member = member, onRemove = { viewModel.removeFamilyMember(member) })
                }
            }
        }
    }

    if (showAddReminderDialog) {
        var medName by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("1 Tablet") }
        var times by remember { mutableStateOf("08:00 AM, 08:00 PM") }
        var days by remember { mutableStateOf("5") }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text("Add Medicine Reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = medName, onValueChange = { medName = it }, label = { Text("Medicine Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = times, onValueChange = { times = it }, label = { Text("Times (comma separated)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Duration (days)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank()) {
                            viewModel.addReminder(medName, dosage, times, days.toIntOrNull() ?: 5)
                            showAddReminderDialog = false
                        }
                    }
                ) {
                    Text("Save Reminder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddFamilyDialog) {
        var name by remember { mutableStateOf("") }
        var relation by remember { mutableStateOf("Parent") }
        var age by remember { mutableStateOf("65") }
        var phone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddFamilyDialog = false },
            title = { Text("Add Family Member") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = relation, onValueChange = { relation = it }, label = { Text("Relation (Parent, Child, Spouse)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Emergency Contact") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addFamilyMember(name, relation, age.toIntOrNull() ?: 0, phone)
                            showAddFamilyDialog = false
                        }
                    }
                ) {
                    Text("Add Member")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFamilyDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ReminderItemCard(
    reminder: LocalReminderEntity,
    onTaken: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(reminder.medicineName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${reminder.dosage} • ${reminder.reminderTimesJson}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = if (reminder.isTakenToday) "✓ Taken Today" else "Pending Today",
                    color = if (reminder.isTakenToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!reminder.isTakenToday) {
                    FilledTonalButton(onClick = onTaken) {
                        Text("Mark Taken")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun FamilyMemberCard(member: LocalFamilyMemberEntity, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(member.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("${member.relation} • Age: ${member.age} • Ph: ${member.emergencyContact}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}

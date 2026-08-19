package com.example.presentation.screens.health

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LocalFamilyMemberEntity
import com.example.data.local.entity.LocalReminderEntity
import com.example.presentation.common.*
import com.example.presentation.viewmodel.ReminderFamilyViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: ReminderFamilyViewModel,
    onNavigateToApiConfig: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val isSeniorMode by viewModel.isSeniorMode.collectAsState()
    val isAlarmRinging by viewModel.isAlarmRinging.collectAsState()
    val activeAlarmMedicine by viewModel.activeAlarmMedicine.collectAsState()

    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showAddFamilyDialog by remember { mutableStateOf(false) }

    // Live Local Clock State (Updates every second)
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Health & Medicine Alarms",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Automated dose reminders & senior accessibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.triggerTestAlarmNow() },
                        modifier = Modifier.testTag("btn_test_alarm_sound")
                    ) {
                        Icon(
                            Icons.Default.AlarmOn,
                            contentDescription = "Test Alarm Ringing",
                            tint = AccentHealth
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
            // Active Alarm Alert Banner (When Alarm is Ringing)
            if (isAlarmRinging) {
                item {
                    ActiveAlarmRingingBanner(
                        medicineName = activeAlarmMedicine ?: "Prescribed Dose",
                        onStopAlarm = { viewModel.stopActiveAlarm() }
                    )
                }
            }

            // Real-Time Local Clock Banner
            item {
                LocalClockBanner(
                    timeString = currentTimeString,
                    dateString = currentDateString,
                    onTestAlarm = { viewModel.triggerTestAlarmNow() }
                )
            }

            item {
                MedicalDisclaimerBanner()
            }

            // Senior Citizen Accessibility Mode Toggle
            item {
                MediCard(
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentHealthBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccessibilityNew,
                                    contentDescription = null,
                                    tint = AccentHealth,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(MediSpacing.sm))
                            Column {
                                Text(
                                    "Senior Citizen Mode",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Large text, simplified touch navigation & loud alarms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isSeniorMode,
                            onCheckedChange = { viewModel.toggleSeniorMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentHealth
                            ),
                            modifier = Modifier.testTag("switch_senior_mode")
                        )
                    }
                }
            }

            // Medicine Reminders Section Header
            item {
                MediSectionHeader(
                    title = "Scheduled Medicine Alarms",
                    subtitle = "Alarms ring automatically on your device clock",
                    accentColor = AccentHealth,
                    actionText = "+ Add Alarm",
                    onActionClick = { showAddReminderDialog = true }
                )
            }

            if (reminders.isEmpty()) {
                item {
                    MediCard(
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(MediSpacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = AccentHealth,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(MediSpacing.sm))
                            Text(
                                "No medicine alarms scheduled.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Set your morning, afternoon, or night medicine clock.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(MediSpacing.md))
                            MediPrimaryButton(
                                text = "Schedule Medicine Alarm",
                                onClick = { showAddReminderDialog = true },
                                icon = Icons.Default.Add
                            )
                        }
                    }
                }
            } else {
                items(reminders) { reminder ->
                    ReminderItemCard(
                        reminder = reminder,
                        onTaken = { viewModel.markDoseTaken(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder) },
                        onTestRing = { viewModel.triggerTestAlarmNow(reminder.medicineName, reminder.dosage) }
                    )
                }
            }

            // Family Health Section
            item {
                MediSectionHeader(
                    title = "Family Health Managers",
                    subtitle = "Manage care and alerts for dependents",
                    accentColor = AccentHealth,
                    actionText = "+ Add Member",
                    onActionClick = { showAddFamilyDialog = true }
                )
            }

            if (familyMembers.isEmpty()) {
                item {
                    Text(
                        "No family members added. Add parents or dependents to manage their appointments and care.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(familyMembers) { member ->
                    FamilyMemberCard(member = member, onRemove = { viewModel.removeFamilyMember(member) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddReminderDialog) {
        var medName by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("1 Tablet") }
        var selectedHour by remember { mutableStateOf(8) }
        var selectedMinute by remember { mutableStateOf(0) }
        var selectedAmPm by remember { mutableStateOf("AM") }
        var days by remember { mutableStateOf("5") }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = AccentHealth)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Medicine Alarm Clock", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MediOutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Medicine Name (e.g. Paracetamol)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_alarm_med_name"),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    )
                    MediOutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage (e.g. 500mg / 1 Capsule)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    )

                    Text(
                        "Alarm Time (Local Device Clock):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )

                    // Quick Time Preset Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = (selectedHour == 8 && selectedMinute == 0 && selectedAmPm == "AM"),
                            onClick = { selectedHour = 8; selectedMinute = 0; selectedAmPm = "AM" },
                            label = { Text("8:00 AM (Morning)") }
                        )
                        FilterChip(
                            selected = (selectedHour == 2 && selectedMinute == 0 && selectedAmPm == "PM"),
                            onClick = { selectedHour = 2; selectedMinute = 0; selectedAmPm = "PM" },
                            label = { Text("2:00 PM (Lunch)") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = (selectedHour == 9 && selectedMinute == 0 && selectedAmPm == "PM"),
                            onClick = { selectedHour = 9; selectedMinute = 0; selectedAmPm = "PM" },
                            label = { Text("9:00 PM (Night)") }
                        )
                    }

                    // Custom Clock Stepper
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(MediCornerRadius.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HOUR", style = MaterialTheme.typography.labelSmall)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { selectedHour = if (selectedHour <= 1) 12 else selectedHour - 1 }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Minus Hour")
                                    }
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d", selectedHour),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    )
                                    IconButton(onClick = { selectedHour = if (selectedHour >= 12) 1 else selectedHour + 1 }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Hour")
                                    }
                                }
                            }

                            Text(":", fontWeight = FontWeight.Bold, fontSize = 24.sp)

                            // Minute
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MIN", style = MaterialTheme.typography.labelSmall)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { selectedMinute = if (selectedMinute <= 0) 55 else selectedMinute - 5 }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Minus Min")
                                    }
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d", selectedMinute),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    )
                                    IconButton(onClick = { selectedMinute = if (selectedMinute >= 55) 0 else selectedMinute + 5 }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Min")
                                    }
                                }
                            }

                            // AM/PM Toggle
                            FilledTonalButton(
                                onClick = { selectedAmPm = if (selectedAmPm == "AM") "PM" else "AM" },
                                shape = RoundedCornerShape(MediCornerRadius.sm)
                            ) {
                                Text(selectedAmPm, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    MediOutlinedTextField(
                        value = days,
                        onValueChange = { days = it },
                        label = { Text("Course Duration (Days)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank()) {
                            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", selectedHour, selectedMinute, selectedAmPm)
                            viewModel.addReminder(medName, dosage, formattedTime, days.toIntOrNull() ?: 5)
                            showAddReminderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentHealth),
                    shape = RoundedCornerShape(MediCornerRadius.sm),
                    modifier = Modifier.testTag("btn_save_alarm")
                ) {
                    Text("Set Exact Alarm", color = Color.White, fontWeight = FontWeight.Bold)
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
            title = { Text("Add Family Member", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MediOutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    )
                    MediOutlinedTextField(
                        value = relation,
                        onValueChange = { relation = it },
                        label = { Text("Relation (Parent, Child, Spouse)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    )
                    MediOutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    )
                    MediOutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Emergency Contact") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addFamilyMember(name, relation, age.toIntOrNull() ?: 0, phone)
                            showAddFamilyDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentHealth),
                    shape = RoundedCornerShape(MediCornerRadius.sm)
                ) {
                    Text("Add Member", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFamilyDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun LocalClockBanner(
    timeString: String,
    dateString: String,
    onTestAlarm: () -> Unit
) {
    MediCard(
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = AccentHealth)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "LOCAL TIME CLOCK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentHealth
                    )
                }
                Surface(
                    color = AccentHealthBg,
                    shape = RoundedCornerShape(MediCornerRadius.pill)
                ) {
                    Text(
                        "Device Synced",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentHealth,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = timeString.ifEmpty { "00:00:00 AM" },
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateString.ifEmpty { "Loading local date..." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MediSpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(MediSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Alarm tone will ring loudly at set time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onTestAlarm,
                    shape = RoundedCornerShape(MediCornerRadius.sm),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_quick_test_alarm")
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AccentHealth
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Ring", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ActiveAlarmRingingBanner(
    medicineName: String,
    onStopAlarm: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MediCornerRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MediError.copy(alpha = alpha * 0.15f)),
        border = BorderStroke(1.5.dp, MediError)
    ) {
        Column(modifier = Modifier.padding(MediSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MediError),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "⏰ MEDICINE ALARM RINGING NOW",
                        fontWeight = FontWeight.ExtraBold,
                        color = MediError,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Time to take: $medicineName",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(MediSpacing.md))
            Button(
                onClick = onStopAlarm,
                modifier = Modifier.fillMaxWidth().testTag("btn_silence_alarm"),
                colors = ButtonDefaults.buttonColors(containerColor = MediError),
                shape = RoundedCornerShape(MediCornerRadius.md)
            ) {
                Icon(Icons.Default.VolumeOff, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Alarm Ringing", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun ReminderItemCard(
    reminder: LocalReminderEntity,
    onTaken: () -> Unit,
    onDelete: () -> Unit,
    onTestRing: () -> Unit
) {
    MediCard(
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        tint = AccentHealth
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        reminder.medicineName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    color = AccentHealthBg,
                    shape = RoundedCornerShape(MediCornerRadius.sm),
                    border = BorderStroke(1.dp, AccentHealth.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = reminder.reminderTimesJson,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentHealth
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Dosage: ${reminder.dosage} • ${reminder.daysRemaining} days left",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            MediStatusBadge(
                statusText = if (reminder.isTakenToday) "✓ Dose Taken Today" else "⏳ Pending Alarm Today",
                statusType = if (reminder.isTakenToday) MediStatusType.SUCCESS else MediStatusType.WARNING
            )

            Spacer(modifier = Modifier.height(MediSpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(MediSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!reminder.isTakenToday) {
                        Button(
                            onClick = onTaken,
                            colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                            shape = RoundedCornerShape(MediCornerRadius.sm),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Mark Taken", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    OutlinedButton(
                        onClick = onTestRing,
                        shape = RoundedCornerShape(MediCornerRadius.sm),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Ring Alarm", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Alarm",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyMemberCard(member: LocalFamilyMemberEntity, onRemove: () -> Unit) {
    MediCard(
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    member.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${member.relation} • Age: ${member.age} • Ph: ${member.emergencyContact}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

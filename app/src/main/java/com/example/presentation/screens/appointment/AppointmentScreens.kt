package com.example.presentation.screens.appointment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.core.network.ApiResult
import com.example.core.notification.LocalNotificationHelper
import com.example.data.remote.dto.AppointmentDto
import com.example.presentation.common.*
import com.example.presentation.viewmodel.AppointmentQueueViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentQueueScreen(
    viewModel: AppointmentQueueViewModel,
    onNavigateToApiConfig: () -> Unit
) {
    val appointmentsState by viewModel.appointmentsState.collectAsState()
    val liveQueueState by viewModel.liveQueueState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Appointments & Live Queue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Digital OP tokens and real-time waiting status",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAppointments() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MediTeal
                        )
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MediTeal,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MediTeal,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "My Bookings",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) MediTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Live Queue Tracker",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) MediTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                when (val state = appointmentsState) {
                    is ApiResult.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MediTeal)
                        }
                    }
                    is ApiResult.Unconfigured -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(MediSpacing.lg),
                            verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
                        ) {
                            item {
                                DemoAppointmentCard(
                                    onTrackQueue = {
                                        selectedTab = 1
                                        viewModel.refreshLiveQueue("Dr. Anil Kumar")
                                    }
                                )
                            }
                            item {
                                ApiUnavailableCard(
                                    serviceName = state.serviceName,
                                    endpointName = state.requiredEndpoint,
                                    onConfigureClick = onNavigateToApiConfig
                                )
                            }
                        }
                    }
                    is ApiResult.Success -> {
                        val list = state.data
                        if (list.isEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(MediSpacing.lg),
                                verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
                            ) {
                                item {
                                    DemoAppointmentCard(
                                        onTrackQueue = {
                                            selectedTab = 1
                                            viewModel.refreshLiveQueue("Dr. Anil Kumar")
                                        }
                                    )
                                }
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(MediSpacing.lg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No additional online bookings found. Book via Hospital Discovery.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(MediSpacing.lg),
                                verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
                            ) {
                                items(list) { appointment ->
                                    AppointmentItemCard(
                                        appointment = appointment,
                                        onTrackQueue = {
                                            selectedTab = 1
                                            viewModel.refreshLiveQueue(appointment.doctorName)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(MediSpacing.lg),
                            verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
                        ) {
                            item {
                                DemoAppointmentCard(
                                    onTrackQueue = {
                                        selectedTab = 1
                                        viewModel.refreshLiveQueue("Dr. Anil Kumar")
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Live Queue Tracker View
                when (val qState = liveQueueState) {
                    is ApiResult.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MediTeal)
                        }
                    }
                    is ApiResult.Success -> {
                        val queue = qState.data
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(MediSpacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MediCard(
                                backgroundColor = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    MediStatusBadge(
                                        statusText = "LIVE CONSULTATION QUEUE",
                                        statusType = MediStatusType.PRIMARY
                                    )
                                    Spacer(modifier = Modifier.height(MediSpacing.md))
                                    Text(
                                        text = queue.doctorName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(MediSpacing.lg))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "Serving Now",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                "#${queue.currentServingToken}",
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MediTeal
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(48.dp)
                                                .background(MaterialTheme.colorScheme.outline)
                                        )
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "Your Token",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                "#${queue.patientToken}",
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(MediSpacing.lg))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(MediSpacing.md))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Patients Ahead: ${queue.patientsAhead}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "Est. Delay: ~${queue.estimatedWaitMinutes} mins",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MediTeal
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        // Default Demo Live Queue Tracker View
                        DemoLiveQueueView()
                    }
                }
            }
        }
    }
}

@Composable
fun DemoLiveQueueView() {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }
    var hasNotifiedStep2 by remember { mutableStateOf(false) }
    var hasNotifiedStep3 by remember { mutableStateOf(false) }

    LaunchedEffect(step) {
        if (step == 0) {
            delay(5000)
            step = 1
        } else if (step == 1) {
            delay(5000)
            step = 2
            if (!hasNotifiedStep2) {
                hasNotifiedStep2 = true
                LocalNotificationHelper.showQueueNotification(
                    context = context,
                    title = "MediBridge Queue Alert",
                    message = "Your turn is coming soon. 1 person is ahead of you.",
                    notificationId = 1002
                )
            }
        } else if (step == 2) {
            delay(5000)
            step = 3
            if (!hasNotifiedStep3) {
                hasNotifiedStep3 = true
                LocalNotificationHelper.showQueueNotification(
                    context = context,
                    title = "Doctor Consultation Ready",
                    message = "It's your turn. Please proceed to the doctor.",
                    notificationId = 1003
                )
            }
        }
    }

    val bannerText = when (step) {
        0 -> "3 people ahead of you"
        1 -> "2 people ahead of you"
        2 -> "1 person ahead of you"
        else -> "Your turn!"
    }

    val bannerBgColor by animateColorAsState(
        targetValue = if (step == 3) MediTeal else MediTealLight,
        label = "bannerBg"
    )
    val bannerTextColor = if (step == 3) Color.White else MediTealDark

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(MediSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE QUEUE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MediTeal
                    )
                    Text(
                        text = "Dr. Anil Kumar • Room 104",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(MediCornerRadius.pill)
                ) {
                    Text(
                        text = "Live Simulation",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Prominent Patient Ahead Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MediCornerRadius.lg),
                colors = CardDefaults.cardColors(containerColor = bannerBgColor),
                border = BorderStroke(1.dp, MediTeal.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = MediElevation.subtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MediSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (step == 3) Icons.Default.CheckCircle else Icons.Default.Groups,
                        contentDescription = null,
                        tint = bannerTextColor,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(MediSpacing.sm))
                    Text(
                        text = bannerText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = bannerTextColor
                    )
                    if (step == 3) {
                        Spacer(modifier = Modifier.height(MediSpacing.xs))
                        Text(
                            text = "Please proceed to the doctor room now",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = bannerTextColor
                        )
                    }
                }
            }
        }

        // Live Queue List
        item {
            MediSectionHeader(
                title = "Current Queue Order",
                accentColor = MediTeal
            )
        }

        // 1. Chaitanya
        item {
            QueuePatientCard(
                position = 1,
                name = "Chaitanya",
                isYou = false,
                isCompleted = step >= 1,
                isCurrent = step == 0
            )
        }

        // 2. Mani
        item {
            QueuePatientCard(
                position = 2,
                name = "Mani",
                isYou = false,
                isCompleted = step >= 2,
                isCurrent = step == 1
            )
        }

        // 3. Vignesh
        item {
            QueuePatientCard(
                position = 3,
                name = "Vignesh",
                isYou = false,
                isCompleted = step >= 3,
                isCurrent = step == 2
            )
        }

        // 4. Ricky (You)
        item {
            QueuePatientCard(
                position = 4,
                name = "Ricky",
                isYou = true,
                isCompleted = false,
                isCurrent = step == 3
            )
        }

        item {
            Spacer(modifier = Modifier.height(MediSpacing.sm))
            MediSecondaryButton(
                text = "Restart Demo Queue Simulation",
                onClick = {
                    step = 0
                    hasNotifiedStep2 = false
                    hasNotifiedStep3 = false
                },
                icon = Icons.Default.Replay
            )
        }
    }
}

@Composable
fun QueuePatientCard(
    position: Int,
    name: String,
    isYou: Boolean,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val cardContainerColor = when {
        isCurrent -> MediTealLight
        isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        isYou -> AccentApptBg
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isCurrent -> MediTeal.copy(alpha = 0.5f)
        isYou -> AccentAppt.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MediCornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MediSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) MediSuccessBg
                            else if (isCurrent) MediTeal
                            else MediNavyContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MediSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "$position",
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color.White else MediNavyDark,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(MediSpacing.md))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            fontWeight = if (isYou || isCurrent) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        if (isYou) {
                            Spacer(modifier = Modifier.width(MediSpacing.sm))
                            Surface(
                                color = MediTeal,
                                shape = RoundedCornerShape(MediCornerRadius.pill)
                            ) {
                                Text(
                                    text = "YOU",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Status Badge
            when {
                isCompleted -> MediStatusBadge("Completed ✓", MediStatusType.SUCCESS)
                isCurrent && isYou -> MediStatusBadge("Your Turn Now!", MediStatusType.YOUR_TURN)
                isCurrent -> MediStatusBadge("In Consultation", MediStatusType.PRIMARY)
                isYou -> MediStatusBadge("Waiting", MediStatusType.WAITING)
                else -> MediStatusBadge("Waiting", MediStatusType.NEUTRAL)
            }
        }
    }
}

@Composable
fun DemoAppointmentCard(onTrackQueue: () -> Unit) {
    MediCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("appointment_card_demo")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Dr. Anil Kumar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(MediSpacing.sm))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(MediCornerRadius.pill)
                    ) {
                        Text(
                            "Demo OP",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "General Physician",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediTeal,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Government General Hospital",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Karimnagar, Telangana",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = MediTealLight,
                shape = RoundedCornerShape(MediCornerRadius.md),
                border = BorderStroke(1.dp, MediTeal.copy(alpha = 0.3f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Token",
                        style = MaterialTheme.typography.labelSmall,
                        color = MediTealDark
                    )
                    Text(
                        "D-027",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MediTealDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MediSpacing.md))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(MediSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "25 Aug 2026 • 10:30 AM",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                MediStatusBadge(
                    statusText = "Status: Confirmed",
                    statusType = MediStatusType.CONFIRMED
                )
            }
            Button(
                onClick = onTrackQueue,
                colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                shape = RoundedCornerShape(MediCornerRadius.sm),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.QueryBuilder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Track Queue", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun AppointmentItemCard(appointment: AppointmentDto, onTrackQueue: () -> Unit) {
    MediCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("appointment_card_${appointment.appointmentId}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    appointment.doctorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${appointment.department} • ${appointment.hospitalName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MediTealLight,
                shape = RoundedCornerShape(MediCornerRadius.md),
                border = BorderStroke(1.dp, MediTeal.copy(alpha = 0.3f))
            ) {
                Text(
                    "Token #${appointment.tokenNumber}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color = MediTealDark,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(MediSpacing.md))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(MediSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Slot: ${appointment.slotDateTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onTrackQueue,
                colors = ButtonDefaults.buttonColors(containerColor = MediTeal),
                shape = RoundedCornerShape(MediCornerRadius.sm),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Track Queue", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

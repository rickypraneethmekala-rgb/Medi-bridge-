package com.example.presentation.screens.activity

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccessLogEntity
import com.example.presentation.common.*
import com.example.presentation.viewmodel.MyDocumentsViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentActivityScreen(
    documentsViewModel: MyDocumentsViewModel
) {
    val accessLogs by documentsViewModel.accessLogs.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredLogs = remember(accessLogs, selectedFilter) {
        when (selectedFilter) {
            "SHARED" -> accessLogs.filter { it.action.contains("GRANTED", ignoreCase = true) || it.action.contains("SHARE", ignoreCase = true) }
            "ACCESSED" -> accessLogs.filter { it.action.contains("ACCESSED", ignoreCase = true) || it.action.contains("VIEW", ignoreCase = true) }
            "REVOKED" -> accessLogs.filter { it.action.contains("REVOKED", ignoreCase = true) }
            "ADDED" -> accessLogs.filter { it.action.contains("ADDED", ignoreCase = true) }
            else -> accessLogs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Recent Activity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Immutable audit log of patient actions and record access",
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
                .testTag("screen_recent_activity"),
            contentPadding = PaddingValues(MediSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MediSpacing.md)
        ) {
            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${accessLogs.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MediTealLight,
                            selectedLabelColor = MediTealDark
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "ADDED",
                        onClick = { selectedFilter = "ADDED" },
                        label = { Text("Added", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDCFCE7),
                            selectedLabelColor = Color(0xFF166534)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "SHARED",
                        onClick = { selectedFilter = "SHARED" },
                        label = { Text("Shared", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE0F2FE),
                            selectedLabelColor = Color(0xFF0369A1)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "ACCESSED",
                        onClick = { selectedFilter = "ACCESSED" },
                        label = { Text("Accessed", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEF3C7),
                            selectedLabelColor = Color(0xFFB45309)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "REVOKED",
                        onClick = { selectedFilter = "REVOKED" },
                        label = { Text("Revoked", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEE2E2),
                            selectedLabelColor = Color(0xFF991B1B)
                        )
                    )
                }
            }

            // Summary banner
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(MediCornerRadius.md),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MediTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Every record upload, share, provider view, and revocation is recorded cryptographically on this device.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // List of activity logs
            if (filteredLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MediCornerRadius.md),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No activity matching this filter.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filteredLogs) { log ->
                    val (badgeText, badgeBg, badgeTextColor, icon) = when {
                        log.action.contains("REVOKED", ignoreCase = true) -> {
                            Quadruple("ACCESS REVOKED", Color(0xFFFEE2E2), Color(0xFF991B1B), Icons.Default.Cancel)
                        }
                        log.action.contains("GRANTED", ignoreCase = true) || log.action.contains("SHARE", ignoreCase = true) -> {
                            Quadruple("RECORD SHARED", Color(0xFFE0F2FE), Color(0xFF0369A1), Icons.Default.Share)
                        }
                        log.action.contains("ACCESSED", ignoreCase = true) -> {
                            Quadruple("RECORD ACCESSED", Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.Visibility)
                        }
                        else -> {
                            Quadruple("RECORD ADDED", Color(0xFFDCFCE7), Color(0xFF166534), Icons.Default.AddCircle)
                        }
                    }

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
                                color = badgeBg,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = badgeTextColor, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = log.documentTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        color = badgeBg,
                                        shape = RoundedCornerShape(MediCornerRadius.pill)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            color = badgeTextColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Target / Accessor: ${log.accessorName} (${log.accessorRole})",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

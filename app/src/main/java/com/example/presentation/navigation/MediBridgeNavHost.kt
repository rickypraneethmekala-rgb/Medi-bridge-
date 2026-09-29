package com.example.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.security.UserRole
import com.example.presentation.screens.activity.RecentActivityScreen
import com.example.presentation.screens.appointment.AppointmentQueueScreen
import com.example.presentation.screens.auth.AuthScreen
import com.example.presentation.screens.documents.MyDocumentsScreen
import com.example.presentation.screens.emergency.EmergencyBottomSheet
import com.example.presentation.screens.emergency.EmergencyHealthInfoScreen
import com.example.presentation.screens.health.HealthScreen
import com.example.presentation.screens.home.HomeScreen
import com.example.presentation.screens.hospital.HospitalDiscoveryScreen
import com.example.presentation.screens.medicine.MedicinePharmacyScreen
import com.example.presentation.screens.order.OrderDeliveryScreen
import com.example.presentation.screens.portals.RolePortalScreen
import com.example.presentation.screens.prescription.PrescriptionAiScreen
import com.example.presentation.screens.settings.ApiConfigScreen
import com.example.presentation.screens.summary.MyHealthSummaryScreen
import com.example.presentation.viewmodel.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null, val testTag: String = "") {
    data object Auth : Screen("auth", "Sign In")
    data object Home : Screen("home", "Home", Icons.Default.Home, "bottom_tab_home")
    data object HealthSummary : Screen("health_summary", "My Health Summary", Icons.Default.Assessment, "bottom_tab_summary")
    data object EmergencyInfo : Screen("emergency_info", "Emergency Health Information", Icons.Default.HealthAndSafety, "bottom_tab_emergency")
    data object RecentActivity : Screen("recent_activity", "Recent Activity", Icons.Default.History, "bottom_tab_activity")
    data object ApiConfig : Screen("api_config", "Settings", Icons.Default.Settings, "bottom_tab_settings")

    // Full-screen features opened from Home feature cards
    data object Hospitals : Screen("hospitals", "Find Doctor & Hospital")
    data object Appointments : Screen("appointments", "Book & Live Queue")
    data object Medicines : Screen("medicines", "Pharmacy & Orders")
    data object AiAssistant : Screen("ai_assistant", "AI Health Assistant")
    data object HealthRecords : Screen("health_records", "My Documents")
    data object Documents : Screen("documents", "My Documents")
    data object Health : Screen("health", "Medicine Reminders")
    data object Orders : Screen("orders", "Orders")
    data object RolePortal : Screen("role_portal", "Portal")
}

// Exactly 5 bottom tabs: Home | My Health Summary | Emergency Health Information | Recent Activity | Settings
val patientBottomTabs = listOf(
    Screen.Home,
    Screen.HealthSummary,
    Screen.EmergencyInfo,
    Screen.RecentActivity,
    Screen.ApiConfig
)

@Composable
fun MediBridgeNavHost(
    authViewModel: AuthViewModel = viewModel(),
    hospitalViewModel: HospitalDoctorViewModel = viewModel(),
    appointmentViewModel: AppointmentQueueViewModel = viewModel(),
    documentsViewModel: MyDocumentsViewModel = viewModel(),
    medicineViewModel: MedicinePharmacyViewModel = viewModel(),
    orderViewModel: OrderDeliveryViewModel = viewModel(),
    prescriptionAiViewModel: PrescriptionAiViewModel = viewModel(),
    reminderViewModel: ReminderFamilyViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isSeniorMode by reminderViewModel.isSeniorMode.collectAsState()
    var showEmergencyModal by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isPatient = currentUser?.role == UserRole.PATIENT || currentUser == null

    Scaffold(
        bottomBar = {
            if (currentUser != null && currentRoute != Screen.Auth.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    if (isPatient) {
                        patientBottomTabs.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = screen.icon!!,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(19.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 8.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        softWrap = true,
                                        lineHeight = 9.5.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                alwaysShowLabel = true,
                                selected = isSelected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = com.example.ui.theme.MediTeal,
                                    selectedTextColor = com.example.ui.theme.MediTeal,
                                    indicatorColor = com.example.ui.theme.MediTealLight,
                                    unselectedIconColor = com.example.ui.theme.MediTextSecondary,
                                    unselectedTextColor = com.example.ui.theme.MediTextSecondary
                                ),
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                modifier = Modifier.testTag(screen.testTag)
                            )
                        }
                    } else {
                        // Non-patient role (Doctor, Pharmacist, Delivery Partner, Admin)
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Portal", modifier = Modifier.size(22.dp)) },
                            label = { Text("Portal", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            selected = currentRoute == Screen.RolePortal.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = com.example.ui.theme.MediTeal,
                                selectedTextColor = com.example.ui.theme.MediTeal,
                                indicatorColor = com.example.ui.theme.MediTealLight,
                                unselectedIconColor = com.example.ui.theme.MediTextSecondary,
                                unselectedTextColor = com.example.ui.theme.MediTextSecondary
                            ),
                            onClick = { navController.navigate(Screen.RolePortal.route) }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(22.dp)) },
                            label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            selected = currentRoute == Screen.ApiConfig.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = com.example.ui.theme.MediTeal,
                                selectedTextColor = com.example.ui.theme.MediTeal,
                                indicatorColor = com.example.ui.theme.MediTealLight,
                                unselectedIconColor = com.example.ui.theme.MediTextSecondary,
                                unselectedTextColor = com.example.ui.theme.MediTextSecondary
                            ),
                            onClick = { navController.navigate(Screen.ApiConfig.route) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser == null) Screen.Auth.route else (if (isPatient) Screen.Home.route else Screen.RolePortal.route),
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    authViewModel = authViewModel,
                    onAuthSuccess = {
                        val role = authViewModel.currentUser.value?.role ?: UserRole.PATIENT
                        if (role == UserRole.PATIENT) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.RolePortal.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    currentUser = currentUser,
                    isSeniorMode = isSeniorMode,
                    onNavigateToFindDoctorHospital = { navController.navigate(Screen.Hospitals.route) },
                    onNavigateToBookLiveQueue = { navController.navigate(Screen.Appointments.route) },
                    onNavigateToAiAssistant = { navController.navigate(Screen.AiAssistant.route) },
                    onNavigateToPharmacyOrders = { navController.navigate(Screen.Medicines.route) },
                    onNavigateToMedicineReminders = { navController.navigate(Screen.Health.route) },
                    onNavigateToMyDocuments = { navController.navigate(Screen.HealthRecords.route) }
                )
            }

            composable(Screen.HealthSummary.route) {
                MyHealthSummaryScreen(
                    documentsViewModel = documentsViewModel,
                    appointmentViewModel = appointmentViewModel,
                    onNavigateToRecords = { navController.navigate(Screen.HealthRecords.route) },
                    onNavigateToAppointments = { navController.navigate(Screen.Appointments.route) }
                )
            }

            composable(Screen.EmergencyInfo.route) {
                EmergencyHealthInfoScreen(
                    documentsViewModel = documentsViewModel,
                    onTriggerSOS = { showEmergencyModal = true }
                )
            }

            composable(Screen.RecentActivity.route) {
                RecentActivityScreen(
                    documentsViewModel = documentsViewModel
                )
            }

            composable(Screen.Hospitals.route) {
                HospitalDiscoveryScreen(
                    viewModel = hospitalViewModel,
                    onDoctorSelected = { doctor, hospital ->
                        appointmentViewModel.bookAppointment(
                            hospitalId = hospital.id,
                            doctorId = doctor.id,
                            department = doctor.department,
                            slotDateTime = doctor.nextAvailableSlot,
                            patientName = currentUser?.fullName ?: "Patient"
                        )
                        navController.navigate(Screen.Appointments.route)
                    },
                    onBack = { navController.popBackStack() },
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.Appointments.route) {
                AppointmentQueueScreen(
                    viewModel = appointmentViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.Medicines.route) {
                MedicinePharmacyScreen(
                    viewModel = medicineViewModel,
                    orderViewModel = orderViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.AiAssistant.route) {
                PrescriptionAiScreen(
                    viewModel = prescriptionAiViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.HealthRecords.route) {
                MyDocumentsScreen(
                    viewModel = documentsViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.Documents.route) {
                MyDocumentsScreen(
                    viewModel = documentsViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.Health.route) {
                HealthScreen(
                    viewModel = reminderViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.Orders.route) {
                OrderDeliveryScreen(
                    viewModel = orderViewModel,
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.RolePortal.route) {
                currentUser?.let { user ->
                    RolePortalScreen(
                        user = user,
                        orderViewModel = orderViewModel,
                        onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) },
                        onSwitchRole = { newRole ->
                            authViewModel.switchRoleForDemo(newRole)
                        }
                    )
                }
            }

            composable(Screen.ApiConfig.route) {
                ApiConfigScreen(
                    authViewModel = authViewModel,
                    currentUser = currentUser,
                    documentsViewModel = documentsViewModel,
                    onBack = {
                        if (currentUser != null) {
                            if (isPatient) navController.navigate(Screen.Home.route) else navController.navigate(Screen.RolePortal.route)
                        } else {
                            navController.navigate(Screen.Auth.route)
                        }
                    }
                )
            }
        }

        if (showEmergencyModal) {
            EmergencyBottomSheet(onDismiss = { showEmergencyModal = false })
        }
    }
}

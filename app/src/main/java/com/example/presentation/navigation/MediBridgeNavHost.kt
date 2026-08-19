package com.example.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.security.UserRole
import com.example.presentation.screens.appointment.AppointmentQueueScreen
import com.example.presentation.screens.auth.AuthScreen
import com.example.presentation.screens.emergency.EmergencyBottomSheet
import com.example.presentation.screens.health.HealthScreen
import com.example.presentation.screens.home.HomeScreen
import com.example.presentation.screens.hospital.HospitalDiscoveryScreen
import com.example.presentation.screens.medicine.MedicinePharmacyScreen
import com.example.presentation.screens.order.OrderDeliveryScreen
import com.example.presentation.screens.portals.RolePortalScreen
import com.example.presentation.screens.prescription.PrescriptionAiScreen
import com.example.presentation.screens.settings.ApiConfigScreen
import com.example.presentation.viewmodel.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Auth : Screen("auth", "Sign In")
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Hospitals : Screen("hospitals", "Hospitals", Icons.Default.LocalHospital)
    data object Appointments : Screen("appointments", "Appointments", Icons.Default.EventAvailable)
    data object Medicines : Screen("medicines", "Medicines", Icons.Default.Medication)
    data object Prescriptions : Screen("prescriptions", "Prescriptions", Icons.Default.DocumentScanner)
    data object Health : Screen("health", "Health", Icons.Default.Favorite)
    data object Orders : Screen("orders", "Orders", Icons.Default.LocalShipping)
    data object RolePortal : Screen("role_portal", "Portal", Icons.Default.Dashboard)
    data object ApiConfig : Screen("api_config", "Settings", Icons.Default.Settings)
}

val patientBottomTabs = listOf(
    Screen.Home,
    Screen.Appointments,
    Screen.Medicines,
    Screen.Health,
    Screen.ApiConfig
)

@Composable
fun MediBridgeNavHost(
    authViewModel: AuthViewModel = viewModel(),
    hospitalViewModel: HospitalDoctorViewModel = viewModel(),
    appointmentViewModel: AppointmentQueueViewModel = viewModel(),
    prescriptionViewModel: PrescriptionAiViewModel = viewModel(),
    medicineViewModel: MedicinePharmacyViewModel = viewModel(),
    orderViewModel: OrderDeliveryViewModel = viewModel(),
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
                NavigationBar {
                    if (isPatient) {
                        patientBottomTabs.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    } else {
                        // Non-patient role (Doctor, Pharmacist, Delivery Partner, Admin)
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Portal") },
                            label = { Text("Portal") },
                            selected = currentRoute == Screen.RolePortal.route,
                            onClick = { navController.navigate(Screen.RolePortal.route) }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("API Settings") },
                            selected = currentRoute == Screen.ApiConfig.route,
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
                    onNavigateToHospitals = { navController.navigate(Screen.Hospitals.route) },
                    onNavigateToAppointments = { navController.navigate(Screen.Appointments.route) },
                    onNavigateToMedicines = { navController.navigate(Screen.Medicines.route) },
                    onNavigateToHealthAssistant = { navController.navigate(Screen.Health.route) },
                    onNavigateToReminders = { navController.navigate(Screen.Health.route) },
                    onEmergencyTrigger = { showEmergencyModal = true }
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
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.Appointments.route) {
                AppointmentQueueScreen(
                    viewModel = appointmentViewModel,
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.Medicines.route) {
                MedicinePharmacyScreen(
                    viewModel = medicineViewModel,
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.Prescriptions.route) {
                PrescriptionAiScreen(
                    viewModel = prescriptionViewModel,
                    onNavigateToApiConfig = { navController.navigate(Screen.ApiConfig.route) }
                )
            }

            composable(Screen.Health.route) {
                HealthScreen(
                    viewModel = reminderViewModel,
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

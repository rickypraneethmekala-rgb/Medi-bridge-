package com.example.presentation.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.network.ApiResult
import com.example.core.security.UserRole
import com.example.presentation.common.*
import com.example.presentation.viewmodel.AuthViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.PATIENT) }
    var licenseNumber by remember { mutableStateOf("") }
    var showRoleDropdown by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is ApiResult.Success) {
            onAuthSuccess()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(MediSpacing.xl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_medibridge_logo),
                contentDescription = "MediBridge Logo",
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(MediCornerRadius.md))
            )
            Spacer(modifier = Modifier.height(MediSpacing.md))
            Text(
                text = "MediBridge+",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(MediSpacing.xs))
            Text(
                text = if (isRegisterMode) "Create your verified healthcare account" else "Sign in to access healthcare services",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MediSpacing.xl))

            // Role Selector Card
            MediCard(
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    MediSectionHeader(
                        title = "Select Your Role",
                        accentColor = MediTeal
                    )
                    Spacer(modifier = Modifier.height(MediSpacing.xs))
                    Box {
                        OutlinedButton(
                            onClick = { showRoleDropdown = true },
                            modifier = Modifier.fillMaxWidth().testTag("dropdown_role_select"),
                            shape = RoundedCornerShape(MediCornerRadius.md)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedRole.displayName, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MediTeal)
                            }
                        }
                        DropdownMenu(
                            expanded = showRoleDropdown,
                            onDismissRequest = { showRoleDropdown = false }
                        ) {
                            UserRole.entries.forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(role.displayName, fontWeight = FontWeight.Bold)
                                            Text(role.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedRole = role
                                        showRoleDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MediSpacing.md))

            if (isRegisterMode) {
                MediOutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MediTeal) },
                    modifier = Modifier.fillMaxWidth().testTag("input_full_name"),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                )
                Spacer(modifier = Modifier.height(MediSpacing.md))

                MediOutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MediTeal) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_phone"),
                    shape = RoundedCornerShape(MediCornerRadius.md)
                )
                Spacer(modifier = Modifier.height(MediSpacing.md))

                if (selectedRole != UserRole.PATIENT) {
                    MediOutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        label = { Text("License / Registration Number") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MediTeal) },
                        modifier = Modifier.fillMaxWidth().testTag("input_license"),
                        shape = RoundedCornerShape(MediCornerRadius.md)
                    )
                    Spacer(modifier = Modifier.height(MediSpacing.md))
                }
            }

            MediOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MediTeal) },
                modifier = Modifier.fillMaxWidth().testTag("input_email"),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )
            Spacer(modifier = Modifier.height(MediSpacing.md))

            MediOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MediTeal) },
                modifier = Modifier.fillMaxWidth().testTag("input_password"),
                shape = RoundedCornerShape(MediCornerRadius.md)
            )

            if (authState is ApiResult.HttpError) {
                Spacer(modifier = Modifier.height(MediSpacing.sm))
                Text(
                    text = (authState as ApiResult.HttpError).userMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(MediSpacing.xl))

            Button(
                onClick = {
                    if (isRegisterMode) {
                        authViewModel.register(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            password = password,
                            role = selectedRole,
                            regNo = licenseNumber.ifBlank { null }
                        )
                    } else {
                        authViewModel.login(email = email, password = password, role = selectedRole)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MediButtonHeight.standard)
                    .testTag("btn_auth_submit"),
                shape = RoundedCornerShape(MediCornerRadius.md),
                colors = ButtonDefaults.buttonColors(containerColor = MediTeal)
            ) {
                if (authState is ApiResult.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        if (isRegisterMode) "Create Account" else "Sign In",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(MediSpacing.md))

            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    authViewModel.resetAuthState()
                },
                modifier = Modifier.testTag("btn_toggle_auth_mode")
            ) {
                Text(
                    if (isRegisterMode) "Already have an account? Sign In" else "New to MediBridge+? Create Account",
                    color = MediTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

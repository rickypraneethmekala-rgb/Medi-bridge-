package com.example.core.security

enum class UserRole(val displayName: String, val description: String) {
    PATIENT("Patient", "Access appointments, prescriptions, pharmacies, and reminders"),
    DOCTOR("Doctor", "Manage daily consultation queues and write digital prescriptions"),
    PHARMACIST("Pharmacist", "Verify digital prescriptions and prepare medicine orders"),
    DELIVERY_PARTNER("Delivery Partner", "Manage delivery dispatches and verify customer OTPs"),
    HOSPITAL_ADMIN("Hospital Admin", "Manage facility rosters, departments, and doctor schedules"),
    SUPER_ADMIN("Super Admin", "Full system governance, security audit logs, and API oversight")
}

data class AuthUser(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val token: String,
    val refreshToken: String? = null
)

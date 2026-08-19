package com.example.core.security

import android.content.Context
import android.content.SharedPreferences

class SecureStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("medibridge_secure_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_SENIOR_MODE = "senior_mode_enabled"
        private const val KEY_APP_LANGUAGE = "app_language" // "en", "te", "hi"
    }

    fun saveSession(user: AuthUser) {
        prefs.edit()
            .putString(KEY_AUTH_TOKEN, user.token)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.fullName)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_PHONE, user.phone)
            .putString(KEY_USER_ROLE, user.role.name)
            .apply()
    }

    fun getSession(): AuthUser? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null) ?: return null
        val id = prefs.getString(KEY_USER_ID, "") ?: ""
        val name = prefs.getString(KEY_USER_NAME, "User") ?: "User"
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val phone = prefs.getString(KEY_USER_PHONE, "") ?: ""
        val roleStr = prefs.getString(KEY_USER_ROLE, UserRole.PATIENT.name) ?: UserRole.PATIENT.name
        val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.PATIENT }
        return AuthUser(id = id, fullName = name, email = email, phone = phone, role = role, token = token)
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_PHONE)
            .remove(KEY_USER_ROLE)
            .apply()
    }

    var isSeniorMode: Boolean
        get() = prefs.getBoolean(KEY_SENIOR_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_SENIOR_MODE, value).apply()

    var appLanguage: String
        get() = prefs.getString(KEY_APP_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_APP_LANGUAGE, value).apply()
}

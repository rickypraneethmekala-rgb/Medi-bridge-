package com.example.core.config

import android.content.Context
import android.content.SharedPreferences

object ApiConfig {
    private const val PREFS_NAME = "medibridge_api_config"
    private const val KEY_BASE_URL = "api_base_url"
    private const val KEY_AUTH_URL = "api_auth_url"
    private const val KEY_HOSPITAL_URL = "api_hospital_url"
    private const val KEY_DOCTOR_URL = "api_doctor_url"
    private const val KEY_APPOINTMENT_URL = "api_appointment_url"
    private const val KEY_QUEUE_URL = "api_queue_url"
    private const val KEY_PRESCRIPTION_URL = "api_prescription_url"
    private const val KEY_OCR_URL = "api_ocr_url"
    private const val KEY_AI_HEALTH_URL = "api_ai_health_url"
    private const val KEY_MEDICINE_URL = "api_medicine_url"
    private const val KEY_PHARMACY_URL = "api_pharmacy_url"
    private const val KEY_ORDER_URL = "api_order_url"
    private const val KEY_PAYMENT_URL = "api_payment_url"
    private const val KEY_DELIVERY_URL = "api_delivery_url"
    private const val KEY_MAP_URL = "api_map_url"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getBaseUrl(context: Context): String {
        return getPrefs(context).getString(KEY_BASE_URL, "") ?: ""
    }

    fun setBaseUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_BASE_URL, url.trim()).apply()
    }

    fun isConfigured(context: Context): Boolean {
        return getBaseUrl(context).isNotBlank()
    }

    fun getCustomEndpoint(context: Context, key: String): String {
        return getPrefs(context).getString(key, "") ?: ""
    }

    fun setCustomEndpoint(context: Context, key: String, url: String) {
        getPrefs(context).edit().putString(key, url.trim()).apply()
    }

    const val ENDPOINT_AUTH = KEY_AUTH_URL
    const val ENDPOINT_HOSPITAL = KEY_HOSPITAL_URL
    const val ENDPOINT_DOCTOR = KEY_DOCTOR_URL
    const val ENDPOINT_APPOINTMENT = KEY_APPOINTMENT_URL
    const val ENDPOINT_QUEUE = KEY_QUEUE_URL
    const val ENDPOINT_PRESCRIPTION = KEY_PRESCRIPTION_URL
    const val ENDPOINT_OCR = KEY_OCR_URL
    const val ENDPOINT_AI_HEALTH = KEY_AI_HEALTH_URL
    const val ENDPOINT_MEDICINE = KEY_MEDICINE_URL
    const val ENDPOINT_PHARMACY = KEY_PHARMACY_URL
    const val ENDPOINT_ORDER = KEY_ORDER_URL
    const val ENDPOINT_PAYMENT = KEY_PAYMENT_URL
    const val ENDPOINT_DELIVERY = KEY_DELIVERY_URL
    const val ENDPOINT_MAP = KEY_MAP_URL
}

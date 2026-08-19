package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MediBridgeApp
import com.example.core.config.ApiConfig
import com.example.core.network.ApiResult
import com.example.core.network.RetrofitClientProvider
import com.example.core.security.SecureStorage
import com.example.data.remote.api.AIHealthApi
import com.example.data.remote.api.OCRApi
import com.example.data.remote.api.PrescriptionApi
import com.example.data.remote.dto.AiExplanationResponse
import com.example.data.remote.dto.OcrExtractionResponse
import com.example.data.remote.dto.PrescriptionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrescriptionAiViewModel(application: Application) : AndroidViewModel(application) {
    private val secureStorage = SecureStorage(application)
    private val ttsManager = (application as MediBridgeApp).ttsManager

    private val _prescriptionsState = MutableStateFlow<ApiResult<List<PrescriptionDto>>>(ApiResult.Unconfigured("Prescriptions", "PRESCRIPTION_API_URL"))
    val prescriptionsState: StateFlow<ApiResult<List<PrescriptionDto>>> = _prescriptionsState.asStateFlow()

    private val _ocrState = MutableStateFlow<ApiResult<OcrExtractionResponse>?>(null)
    val ocrState: StateFlow<ApiResult<OcrExtractionResponse>?> = _ocrState.asStateFlow()

    private val _aiExplanationState = MutableStateFlow<ApiResult<AiExplanationResponse>?>(null)
    val aiExplanationState: StateFlow<ApiResult<AiExplanationResponse>?> = _aiExplanationState.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(secureStorage.appLanguage)
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        loadPrescriptions()
    }

    fun setLanguage(langCode: String) {
        _selectedLanguage.value = langCode
        secureStorage.appLanguage = langCode
    }

    fun loadPrescriptions() {
        viewModelScope.launch {
            _prescriptionsState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<PrescriptionApi>(context, ApiConfig.ENDPOINT_PRESCRIPTION)
            if (api == null) {
                _prescriptionsState.value = ApiResult.Unconfigured("Prescription Service", "PRESCRIPTION_API_URL")
                return@launch
            }

            val session = secureStorage.getSession()
            val patientId = session?.id ?: "patient_1"
            val result = RetrofitClientProvider.safeApiCall("Prescription") {
                api.getPrescriptions(patientId)
            }
            _prescriptionsState.value = result
        }
    }

    fun processPrescriptionOcr(imageUrl: String) {
        viewModelScope.launch {
            _ocrState.value = ApiResult.Loading
            val context = getApplication<Application>()
            val api = RetrofitClientProvider.createService<OCRApi>(context, ApiConfig.ENDPOINT_OCR)
            if (api == null) {
                _ocrState.value = ApiResult.Unconfigured("Prescription OCR Engine", "OCR_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("OCR Engine") {
                api.extractPrescriptionOcr(imageUrl)
            }
            _ocrState.value = result
        }
    }

    fun requestAiExplanation(medicineName: String, dosage: String) {
        viewModelScope.launch {
            _aiExplanationState.value = ApiResult.Loading
            val context = getApplication<Application>()

            // If Gemini API Key is configured via BuildConfig, use Gemini 3.5 Flash directly
            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                val geminiResult = com.example.core.ai.GeminiHealthService.explainMedicine(
                    medicineName = medicineName,
                    dosage = dosage,
                    language = _selectedLanguage.value
                )
                _aiExplanationState.value = geminiResult
                return@launch
            }

            // Fallback to custom backend AI Health Gateway if configured
            val api = RetrofitClientProvider.createService<AIHealthApi>(context, ApiConfig.ENDPOINT_AI_HEALTH)
            if (api == null) {
                _aiExplanationState.value = ApiResult.Unconfigured("AI Prescription Explanation", "GEMINI_API_KEY or AI_API_URL")
                return@launch
            }

            val result = RetrofitClientProvider.safeApiCall("AI Explanation") {
                api.explainPrescription(medicineName = medicineName, dosage = dosage, language = _selectedLanguage.value)
            }
            _aiExplanationState.value = result
        }
    }

    fun playAudioExplanation(text: String) {
        ttsManager.speak(text, _selectedLanguage.value)
        _isSpeaking.value = true
    }

    fun stopAudio() {
        ttsManager.stop()
        _isSpeaking.value = false
    }

    fun clearOcrState() {
        _ocrState.value = null
    }

    fun clearExplanationState() {
        _aiExplanationState.value = null
        stopAudio()
    }
}

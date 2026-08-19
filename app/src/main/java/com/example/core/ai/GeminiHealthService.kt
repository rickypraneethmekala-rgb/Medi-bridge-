package com.example.core.ai

import com.example.BuildConfig
import com.example.core.network.ApiResult
import com.example.data.remote.dto.AiExplanationResponse
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(val contents: List<GeminiContent>)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent?)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

object GeminiHealthService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun explainMedicine(
        medicineName: String,
        dosage: String,
        language: String
    ): ApiResult<AiExplanationResponse> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ApiResult.Unconfigured(
                serviceName = "Gemini AI Health Assistant",
                requiredEndpoint = "GEMINI_API_KEY"
            )
        }

        val langPrompt = when (language) {
            "te" -> "in Telugu (తెలుగు language script)"
            "hi" -> "in Hindi (हिन्दी language script)"
            else -> "in English"
        }

        val prompt = """
            You are MediBridge Clinical AI Assistant. Explain the prescribed medicine '$medicineName' with dosage '$dosage' $langPrompt.
            Format your response clearly:
            1. General Purpose: What does this medicine treat in simple terms?
            2. Precautions: Important meal or lifestyle precautions.
            3. Voice summary: A single concise sentence suitable for patient text-to-speech audio.
            Do not provide medical diagnosis or alter dosages. Include a physician consultation reminder.
        """.trimIndent()

        try {
            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )
            val response = api.generateContent(apiKey = apiKey, request = request)
            val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Unable to generate medicine explanation at this time."

            val result = AiExplanationResponse(
                medicineName = medicineName,
                language = language,
                generalPurpose = "Clinical summary powered by Gemini 3.5 Flash",
                doctorPrescribedDosage = dosage,
                frequency = "As directed by physician",
                duration = "Follow prescription course",
                precautions = generatedText,
                sideEffectsInfo = "Report any allergic reaction, swelling, or dizziness immediately to your doctor.",
                audioExplanationText = generatedText.take(250).replace("\n", " ")
            )
            ApiResult.Success(result, isLive = true)
        } catch (e: Exception) {
            ApiResult.HttpError(500, e.localizedMessage ?: "Gemini API request failed")
        }
    }
}

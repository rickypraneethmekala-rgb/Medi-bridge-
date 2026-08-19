package com.example.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class MultilingualTtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale.ENGLISH
        } else {
            Log.e("MediBridgeTTS", "Failed to initialize TextToSpeech engine")
        }
    }

    fun speak(text: String, languageCode: String = "en") {
        if (!isInitialized || tts == null) return

        val locale = when (languageCode.lowercase()) {
            "te", "telugu" -> Locale("te", "IN")
            "hi", "hindi" -> Locale("hi", "IN")
            else -> Locale.ENGLISH
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English if target language data is not downloaded on device
            tts?.language = Locale.ENGLISH
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "medibridge_tts_utterance")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

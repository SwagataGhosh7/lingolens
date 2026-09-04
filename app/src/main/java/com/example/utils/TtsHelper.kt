package com.example.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            }
        }
    }

    fun speak(text: String, languageCode: String) {
        if (!isInitialized) return
        
        val locale = when (languageCode.lowercase()) {
            "spanish" -> Locale("es", "ES")
            "french" -> Locale.FRENCH
            "german" -> Locale.GERMAN
            "japanese" -> Locale.JAPANESE
            "italian" -> Locale.ITALIAN
            else -> Locale.US
        }
        
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

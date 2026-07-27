package com.own.remindme.utils.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onCompletion: (() -> Unit)? = null

    init {
        initialize()
    }

    private fun initialize() {
        Log.d("TTSManager", "Initializing TTS Engine...")
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSManager", "Language is not supported or missing data")
            }
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("TTSManager", "TTS started speaking: $utteranceId")
                }
                override fun onDone(utteranceId: String?) {
                    Log.d("TTSManager", "TTS finished speaking: $utteranceId")
                    val callback = onCompletion
                    onCompletion = null
                    callback?.invoke()
                }
                override fun onError(utteranceId: String?) {
                    Log.e("TTSManager", "TTS error: $utteranceId")
                    val callback = onCompletion
                    onCompletion = null
                    callback?.invoke() // Call anyway to avoid getting stuck
                }
            })
            isInitialized = true
            Log.d("TTSManager", "TTS Initialized successfully")
        } else {
            Log.e("TTSManager", "TTS Initialization failed with status: $status")
            isInitialized = false
        }
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        Log.d("TTSManager", "speak() called with: '$text'")
        
        // Stop any current speech
        stop()
        
        this.onCompletion = onFinished

        if (isInitialized && tts != null) {
            val utteranceId = "ai_response_${System.currentTimeMillis()}"
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            
            if (result == TextToSpeech.ERROR) {
                Log.e("TTSManager", "tts.speak returned ERROR")
                onCompletion = null
                onFinished?.invoke()
            } else {
                Log.d("TTSManager", "tts.speak started successfully with ID: $utteranceId")
            }
        } else {
            Log.w("TTSManager", "TTS not initialized yet. Re-initializing and skipping for now.")
            if (tts == null) initialize()
            onCompletion = null
            onFinished?.invoke()
        }
    }

    fun stop() {
        try {
            if (isInitialized) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.e("TTSManager", "Error stopping TTS", e)
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("TTSManager", "Error releasing TTS", e)
        }
    }
}

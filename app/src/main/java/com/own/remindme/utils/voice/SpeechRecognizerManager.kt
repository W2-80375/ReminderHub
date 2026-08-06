package com.own.remindme.utils.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    
    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText = _recognizedText.asStateFlow()

    private var onResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((Int) -> Unit)? = null

    init {
        CoroutineScope(Dispatchers.Main).launch {
            ensureRecognizer()
        }
    }

    private suspend fun ensureRecognizer() {
        if (speechRecognizer == null) {
            withContext(Dispatchers.Main) {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    Log.e("SpeechRecognizer", "Speech recognition NOT available")
                    return@withContext
                }
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(this@SpeechRecognizerManager)
                Log.d("SpeechRecognizer", "SpeechRecognizer created")
            }
        }
    }

    fun startListening(onResult: (String) -> Unit, onError: ((Int) -> Unit)? = null) {
        this.onResultCallback = onResult
        this.onErrorCallback = onError
        
        CoroutineScope(Dispatchers.Main).launch {
            ensureRecognizer()
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                // Add a small buffer for silence detection
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            }
            
            try {
                // Small delay to ensure any previous audio session (like TTS) is fully closed
                kotlinx.coroutines.delay(300)
                speechRecognizer?.startListening(intent)
                _isListening.value = true
                Log.d("SpeechRecognizer", "startListening() called")
            } catch (e: Exception) {
                Log.e("SpeechRecognizer", "Error starting listening", e)
                _isListening.value = false
                onErrorCallback?.invoke(-1)
            }
        }
    }

    fun stopListening() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("SpeechRecognizer", "stopListening() called")
            speechRecognizer?.stopListening()
            _isListening.value = false
        }
    }

    fun cancel() {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("SpeechRecognizer", "cancel() called")
            speechRecognizer?.cancel()
            _isListening.value = false
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d("SpeechRecognizer", "onReadyForSpeech")
    }

    override fun onBeginningOfSpeech() {
        Log.d("SpeechRecognizer", "onBeginningOfSpeech")
        _isListening.value = true
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        Log.d("SpeechRecognizer", "onEndOfSpeech")
        _isListening.value = false
    }

    override fun onError(error: Int) {
        val errorMessage = getErrorText(error)
        Log.e("SpeechRecognizer", "onError: $error ($errorMessage)")
        _isListening.value = false
        
        if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
            CoroutineScope(Dispatchers.Main).launch {
                resetRecognizer()
            }
        }
        
        onErrorCallback?.invoke(error)
    }

    private suspend fun resetRecognizer() {
        withContext(Dispatchers.Main) {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
            ensureRecognizer()
            Log.d("SpeechRecognizer", "Recognizer reset due to error")
        }
    }

    override fun onResults(results: Bundle?) {
        Log.d("SpeechRecognizer", "onResults received")
        _isListening.value = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0]
            Log.d("SpeechRecognizer", "Speech recognized: $text")
            _recognizedText.value = text
            onResultCallback?.invoke(text)
        } else {
            Log.d("SpeechRecognizer", "onResults: No matches found")
            onErrorCallback?.invoke(SpeechRecognizer.ERROR_NO_MATCH)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _recognizedText.value = matches[0]
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun getErrorText(errorCode: Int): String = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No match"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
        SpeechRecognizer.ERROR_SERVER -> "Error from server"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        else -> "Unknown error"
    }

    fun release() {
        CoroutineScope(Dispatchers.Main).launch {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }
}

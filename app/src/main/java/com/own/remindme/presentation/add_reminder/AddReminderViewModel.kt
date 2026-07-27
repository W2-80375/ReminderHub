package com.own.remindme.presentation.add_reminder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority
import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.model.RepeatType
import com.own.remindme.domain.usecase.ReminderUseCases
import com.own.remindme.data.remote.ai.GroqService
import com.own.remindme.utils.voice.TTSManager
import com.own.remindme.utils.voice.SpeechRecognizerManager
import android.util.Log
import android.speech.SpeechRecognizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val reminderUseCases: ReminderUseCases,
    private val groqService: GroqService,
    private val ttsManager: TTSManager,
    private val speechRecognizerManager: SpeechRecognizerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    enum class ConversationState {
        IDLE, LISTENING, PROCESSING, SPEAKING, SUCCESS
    }

    private val _convState = MutableStateFlow(ConversationState.IDLE)
    val convState = _convState.asStateFlow()

    private val _isAiListening = MutableStateFlow(false)
    val isAiListening = _isAiListening.asStateFlow()

    private val _aiRecognizedText = MutableStateFlow("")
    val aiRecognizedText = _aiRecognizedText.asStateFlow()

    private val _aiResponse = MutableStateFlow("")
    val aiResponse = _aiResponse.asStateFlow()

    private val _showSuccessAnimation = MutableStateFlow(false)
    val showSuccessAnimation = _showSuccessAnimation.asStateFlow()

    private var sessionTimerJob: Job? = null
    private var isSessionActive = false

    private val _reminderTitle = mutableStateOf("")
    val reminderTitle: State<String> = _reminderTitle

    private val _reminderDescription = mutableStateOf("")
    val reminderDescription: State<String> = _reminderDescription

    private val _reminderCategory = mutableStateOf(Category.MEDICINE)
    val reminderCategory: State<Category> = _reminderCategory

    private val _reminderTimes = mutableStateOf<List<Long>>(listOf(System.currentTimeMillis()))
    val reminderTimes: State<List<Long>> = _reminderTimes

    private val _reminderRepeatType = mutableStateOf(RepeatType.NONE)
    val reminderRepeatType: State<RepeatType> = _reminderRepeatType

    private val _reminderPriority = mutableStateOf(Priority.MEDIUM)
    val reminderPriority: State<Priority> = _reminderPriority

    private val _expiryDate = mutableStateOf<Long?>(null)
    val expiryDate: State<Long?> = _expiryDate

    private val _attachmentUris = mutableStateOf<List<String>>(emptyList())
    val attachmentUris: State<List<String>> = _attachmentUris

    private var _lastTakenTimestamp = mutableStateOf<Long?>(null)

    private val _currentReminderId = mutableStateOf<Long?>(null)
    val isEditMode: Boolean get() = _currentReminderId.value != null

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        // Sync AI listening state
        viewModelScope.launch {
            speechRecognizerManager.isListening.collect { listening ->
                _isAiListening.value = listening
            }
        }

        savedStateHandle.get<Long>("reminderId")?.let { reminderId ->
            if (reminderId != -1L) {
                viewModelScope.launch {
                    reminderUseCases.getReminder(reminderId)?.also { reminder ->
                        _currentReminderId.value = reminder.id
                        _reminderTitle.value = reminder.title
                        _reminderDescription.value = reminder.description
                        _reminderCategory.value = reminder.category
                        _reminderTimes.value = reminder.reminderTimes
                        _reminderRepeatType.value = reminder.repeatType
                        _reminderPriority.value = reminder.priority
                        _expiryDate.value = reminder.expiryDate
                        _attachmentUris.value = reminder.imageUris
                        _lastTakenTimestamp.value = reminder.lastTakenTimestamp
                    }
                }
            }
        }
    }

    fun onEvent(event: AddReminderEvent) {
        when (event) {
            is AddReminderEvent.EnteredTitle -> _reminderTitle.value = event.value
            is AddReminderEvent.EnteredDescription -> _reminderDescription.value = event.value
            is AddReminderEvent.ChangeCategory -> _reminderCategory.value = event.category
            is AddReminderEvent.AddTime -> _reminderTimes.value = _reminderTimes.value + event.time
            is AddReminderEvent.RemoveTime -> {
                if (_reminderTimes.value.size > 1) {
                    val newList = _reminderTimes.value.toMutableList()
                    newList.removeAt(event.index)
                    _reminderTimes.value = newList
                }
            }
            is AddReminderEvent.UpdateTime -> {
                val newList = _reminderTimes.value.toMutableList()
                newList[event.index] = event.time
                _reminderTimes.value = newList
            }
            is AddReminderEvent.ChangeRepeatType -> _reminderRepeatType.value = event.repeatType
            is AddReminderEvent.ChangePriority -> _reminderPriority.value = event.priority
            is AddReminderEvent.ChangeExpiryDate -> _expiryDate.value = event.date
            is AddReminderEvent.AddAttachment -> _attachmentUris.value = _attachmentUris.value + event.uri
            is AddReminderEvent.AddAttachments -> _attachmentUris.value = _attachmentUris.value + event.uris
            is AddReminderEvent.RemoveAttachment -> _attachmentUris.value = _attachmentUris.value.filter { it != event.uri }
            is AddReminderEvent.SaveReminder -> saveReminder()
            is AddReminderEvent.DeleteReminder -> {
                viewModelScope.launch {
                    try {
                        _currentReminderId.value?.let { id ->
                            val reminder = reminderUseCases.getReminder(id)
                            reminder?.let {
                                reminderUseCases.deleteReminder(it)
                                _eventFlow.emit(UiEvent.DeleteReminder)
                            }
                        }
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(message = e.message ?: "Couldn't delete reminder"))
                    }
                }
            }
            is AddReminderEvent.ToggleAiListening -> {
                if (isSessionActive) {
                    stopAiListening()
                } else {
                    startAiSession()
                }
            }
        }
    }

    private fun startAiSession() {
        Log.d("AddReminderVM", "Starting AI Session")
        isSessionActive = true
        _aiResponse.value = ""
        _aiRecognizedText.value = ""
        groqService.clearChat()
        startSessionTimer()
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (!isSessionActive) return
        
        Log.d("AddReminderVM", "Internal: Start Listening")
        _convState.value = ConversationState.LISTENING
        _aiRecognizedText.value = "Listening..."
        
        speechRecognizerManager.startListening(
            onResult = { text ->
                Log.d("AddReminderVM", "Speech result: $text")
                _aiRecognizedText.value = text
                processAiInput(text)
            },
            onError = { error ->
                handleSpeechError(error)
            }
        )
    }

    private fun handleSpeechError(error: Int) {
        if (!isSessionActive) return
        
        when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> {
                Log.d("AddReminderVM", "No match. Prompting user.")
                speakAndListen("I didn't catch that. Could you say it again?")
            }
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                Log.d("AddReminderVM", "Timeout. Checking if we should continue.")
                // If it's a silent timeout and we've already had interaction, maybe listen again
                // Or just stop to be "professional"
                startListeningInternal() // Professional apps often keep listening if a session is active
            }
            else -> {
                Log.e("AddReminderVM", "Speech error: $error. Stopping session.")
                stopAiListening()
                viewModelScope.launch {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Speech recognition error. Please try again."))
                }
            }
        }
    }

    private fun processAiInput(text: String) {
        if (text.isBlank() || text == "Listening...") return
        
        Log.d("AddReminderVM", "Processing input: $text")
        _convState.value = ConversationState.PROCESSING
        resetSessionTimer()
        
        viewModelScope.launch {
            try {
                val response = groqService.processInput(text)
                handleAiResponse(response)
            } catch (e: Exception) {
                Log.e("AddReminderVM", "Groq error", e)
                speakAndListen("Sorry, I'm having trouble connecting. Let's try again in a moment.")
            }
        }
    }

    private fun handleAiResponse(response: String) {
        val (verbal, json) = parseAiResponse(response)
        _aiResponse.value = verbal

        if (json != null) {
            val status = json.optString("status")
            if (status == "READY") {
                val data = json.optJSONObject("data") ?: JSONObject()
                applyAiData(data)
                
                val missingField = validateReminder()
                if (missingField == null) {
                    _convState.value = ConversationState.SUCCESS
                    _showSuccessAnimation.value = true
                    speakVerbalOnly(verbal) {
                        saveAndFinish()
                    }
                } else {
                    speakAndListen("I have everything except the $missingField. Could you please provide it?")
                }
            } else {
                val msg = json.optString("message", verbal)
                speakAndListen(msg)
            }
        } else {
            speakAndListen(verbal)
        }
    }

    private fun parseAiResponse(response: String): Pair<String, JSONObject?> {
        var verbal = response
        var json: JSONObject? = null
        try {
            val start = response.indexOf("{")
            val end = response.lastIndexOf("}")
            if (start != -1 && end != -1 && end > start) {
                val jsonStr = response.substring(start, end + 1)
                json = JSONObject(jsonStr)
                verbal = response.substring(0, start).trim() + " " + response.substring(end + 1).trim()
            }
        } catch (e: Exception) {
            Log.e("AddReminderVM", "JSON Parse error", e)
        }
        
        verbal = verbal.replace("```json", "").replace("```", "").trim()
        if (verbal.isEmpty() && json != null) {
            verbal = json.optString("message", "Processing...")
        }
        return verbal to json
    }

    private fun validateReminder(): String? = when {
        reminderTitle.value.isBlank() -> "title"
        reminderRepeatType.value == RepeatType.NONE || reminderRepeatType.value == RepeatType.CUSTOM -> "frequency"
        else -> null
    }

    private fun speakAndListen(text: String) {
        _convState.value = ConversationState.SPEAKING
        _aiResponse.value = text
        
        // Add a safety timeout to prevent getting stuck in SPEAKING mode
        val speakTimeoutJob = viewModelScope.launch {
            delay(8000) // Max 8 seconds for TTS
            if (_convState.value == ConversationState.SPEAKING) {
                Log.w("AddReminderVM", "TTS Timeout. Forcing listener start.")
                startListeningInternal()
            }
        }

        ttsManager.speak(text) {
            speakTimeoutJob.cancel()
            viewModelScope.launch {
                delay(300) // Small gap to avoid hearing itself
                if (isSessionActive) {
                    startListeningInternal()
                }
            }
        }
    }

    private fun speakVerbalOnly(text: String, onFinished: () -> Unit) {
        _convState.value = ConversationState.SPEAKING
        ttsManager.speak(text) {
            onFinished()
        }
    }

    private fun saveAndFinish() {
        viewModelScope.launch {
            saveReminder(autoNavigate = false)
            delay(1500)
            stopAiListening()
            _eventFlow.emit(UiEvent.SaveReminder)
        }
    }

    private fun startSessionTimer() {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            delay(45000) // 45 seconds is more "professional" for complex creations
            if (isSessionActive) {
                Log.d("AddReminderVM", "Session timeout")
                stopAiListening()
            }
        }
    }

    private fun resetSessionTimer() {
        if (isSessionActive) startSessionTimer()
    }

    private fun stopAiListening() {
        Log.d("AddReminderVM", "Stopping AI Session")
        isSessionActive = false
        _convState.value = ConversationState.IDLE
        sessionTimerJob?.cancel()
        speechRecognizerManager.stopListening()
        ttsManager.stop()
    }

    private fun saveReminder(autoNavigate: Boolean = true) {
        if (reminderTitle.value.isBlank()) {
            if (!autoNavigate) return // Don't show snackbar if called from AI during success flow
            viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar("Title is mandatory")) }
            return
        }
        
        if (reminderRepeatType.value == RepeatType.NONE || reminderRepeatType.value == RepeatType.CUSTOM) {
            if (!autoNavigate) return
            viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar("Please select a frequency")) }
            return
        }

        viewModelScope.launch {
            try {
                val reminder = Reminder(
                    id = _currentReminderId.value ?: 0,
                    title = reminderTitle.value,
                    description = reminderDescription.value,
                    reminderTimes = reminderTimes.value,
                    category = reminderCategory.value,
                    repeatType = reminderRepeatType.value,
                    priority = reminderPriority.value,
                    completed = false,
                    expiryDate = expiryDate.value,
                    imageUris = attachmentUris.value,
                    lastTakenTimestamp = _lastTakenTimestamp.value
                )
                
                if (_currentReminderId.value != null) {
                    reminderUseCases.updateReminder(reminder)
                } else {
                    reminderUseCases.addReminder(reminder)
                }
                
                if (autoNavigate) {
                    _eventFlow.emit(UiEvent.SaveReminder)
                }
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar(message = e.message ?: "Couldn't save reminder"))
            }
        }
    }

    private fun applyAiData(data: JSONObject) {
        _reminderTitle.value = data.optString("title", _reminderTitle.value)
        _reminderDescription.value = data.optString("description", _reminderDescription.value)
        
        val categoryStr = data.optString("category")
        if (categoryStr.isNotEmpty()) {
            _reminderCategory.value = Category.entries.find { it.name == categoryStr } ?: Category.CUSTOM
        }

        val repeatStr = data.optString("repeatType")
        if (repeatStr.isNotEmpty()) {
            _reminderRepeatType.value = RepeatType.entries.find { it.name == repeatStr } ?: RepeatType.NONE
        }

        val dateStr = data.optString("date")
        val timeStr = data.optString("time")
        
        if (dateStr.isNotEmpty() && timeStr.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val date = sdf.parse("$dateStr $timeStr")
                date?.let {
                    _reminderTimes.value = listOf(it.time)
                }
            } catch (e: Exception) { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // DO NOT release singletons here as they are shared app-wide
        // and we might return to this screen.
        stopAiListening()
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SaveReminder : UiEvent()
        object DeleteReminder : UiEvent()
    }
}

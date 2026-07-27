package com.own.remindme.presentation.add_reminder

import androidx.lifecycle.SavedStateHandle
import com.own.remindme.data.remote.ai.GroqService
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.usecase.*
import com.own.remindme.utils.voice.SpeechRecognizerManager
import com.own.remindme.utils.voice.TTSManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddReminderViewModelTest {

    private lateinit var viewModel: AddReminderViewModel
    private lateinit var reminderUseCases: ReminderUseCases
    private lateinit var getReminderUseCase: GetReminderUseCase
    private lateinit var addReminderUseCase: AddReminderUseCase
    private lateinit var updateReminderUseCase: UpdateReminderUseCase
    private lateinit var deleteReminderUseCase: DeleteReminderUseCase
    private lateinit var getAllRemindersUseCase: GetAllRemindersUseCase
    private lateinit var groqService: GroqService
    private lateinit var ttsManager: TTSManager
    private lateinit var speechRecognizerManager: SpeechRecognizerManager

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getReminderUseCase = mockk()
        addReminderUseCase = mockk()
        updateReminderUseCase = mockk()
        deleteReminderUseCase = mockk()
        getAllRemindersUseCase = mockk()
        groqService = mockk()
        ttsManager = mockk()
        speechRecognizerManager = mockk()

        every { speechRecognizerManager.isListening } returns MutableStateFlow(false)

        reminderUseCases = ReminderUseCases(
            getAllReminders = getAllRemindersUseCase,
            getReminder = getReminderUseCase,
            addReminder = addReminderUseCase,
            updateReminder = updateReminderUseCase,
            deleteReminder = deleteReminderUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `EnteredTitle event updates title state`() {
        viewModel = AddReminderViewModel(
            reminderUseCases,
            groqService,
            ttsManager,
            speechRecognizerManager,
            SavedStateHandle()
        )
        
        viewModel.onEvent(AddReminderEvent.EnteredTitle("Buy Milk"))
        
        assertEquals("Buy Milk", viewModel.reminderTitle.value)
    }

    @Test
    fun `ChangeCategory event updates category state`() {
        viewModel = AddReminderViewModel(
            reminderUseCases,
            groqService,
            ttsManager,
            speechRecognizerManager,
            SavedStateHandle()
        )
        
        viewModel.onEvent(AddReminderEvent.ChangeCategory(Category.SHOPPING))
        
        assertEquals(Category.SHOPPING, viewModel.reminderCategory.value)
    }

    @Test
    fun `AddAttachment event updates attachmentUris state`() {
        viewModel = AddReminderViewModel(
            reminderUseCases,
            groqService,
            ttsManager,
            speechRecognizerManager,
            SavedStateHandle()
        )
        
        val testUri = "content://media/external/images/media/1"
        viewModel.onEvent(AddReminderEvent.AddAttachment(testUri))
        
        assertEquals(true, viewModel.attachmentUris.value.contains(testUri))
    }
}

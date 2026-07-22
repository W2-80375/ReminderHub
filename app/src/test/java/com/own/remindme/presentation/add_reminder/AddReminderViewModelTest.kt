package com.own.remindme.presentation.add_reminder

import androidx.lifecycle.SavedStateHandle
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.usecase.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getReminderUseCase = mockk()
        addReminderUseCase = mockk()
        updateReminderUseCase = mockk()
        deleteReminderUseCase = mockk()
        getAllRemindersUseCase = mockk()

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
        viewModel = AddReminderViewModel(reminderUseCases, SavedStateHandle())
        
        viewModel.onEvent(AddReminderEvent.EnteredTitle("Buy Milk"))
        
        assertEquals("Buy Milk", viewModel.reminderTitle.value)
    }

    @Test
    fun `ChangeCategory event updates category state`() {
        viewModel = AddReminderViewModel(reminderUseCases, SavedStateHandle())
        
        viewModel.onEvent(AddReminderEvent.ChangeCategory(Category.SHOPPING))
        
        assertEquals(Category.SHOPPING, viewModel.reminderCategory.value)
    }

    @Test
    fun `ChangeImageUri event updates imageUri state`() {
        viewModel = AddReminderViewModel(reminderUseCases, SavedStateHandle())
        
        val testUri = "content://media/external/images/media/1"
        viewModel.onEvent(AddReminderEvent.ChangeImageUri(testUri))
        
        assertEquals(testUri, viewModel.imageUri.value)
    }
}

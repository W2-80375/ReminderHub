package com.own.remindme.presentation.home

import com.own.remindme.domain.model.*
import com.own.remindme.domain.repository.UserPreferences
import com.own.remindme.domain.repository.UserPreferencesRepository
import com.own.remindme.domain.usecase.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var reminderUseCases: ReminderUseCases
    private lateinit var getAllRemindersUseCase: GetAllRemindersUseCase
    private lateinit var addReminderUseCase: AddReminderUseCase
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        getAllRemindersUseCase = mockk()
        addReminderUseCase = mockk()
        val getReminderUseCase: GetReminderUseCase = mockk()
        val updateReminderUseCase: UpdateReminderUseCase = mockk()
        val deleteReminderUseCase: DeleteReminderUseCase = mockk()
        userPreferencesRepository = mockk()
        
        reminderUseCases = ReminderUseCases(
            getAllReminders = getAllRemindersUseCase,
            getReminder = getReminderUseCase,
            addReminder = addReminderUseCase,
            updateReminder = updateReminderUseCase,
            deleteReminder = deleteReminderUseCase
        )

        every { getAllRemindersUseCase() } returns flowOf(emptyList())
        every { userPreferencesRepository.userPreferencesFlow } returns flowOf(UserPreferences())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        viewModel = HomeViewModel(reminderUseCases, userPreferencesRepository)
        
        val state = viewModel.uiState.value
        assertEquals("", state.search)
        assertEquals(0, state.selectedCategory)
        assertEquals(0, state.todayReminders.size)
    }

    @Test
    fun `onCategoryClick updates selected category`() = runTest {
        viewModel = HomeViewModel(reminderUseCases, userPreferencesRepository)
        
        viewModel.onCategoryClick(1)
        
        assertEquals(1, viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `onSearchQueryChange updates search state`() = runTest {
        viewModel = HomeViewModel(reminderUseCases, userPreferencesRepository)
        
        viewModel.onSearchQueryChange("test")
        
        assertEquals("test", viewModel.uiState.value.search)
    }
}

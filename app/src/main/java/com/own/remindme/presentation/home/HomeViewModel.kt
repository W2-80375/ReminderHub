package com.own.remindme.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.own.remindme.domain.model.Category as DomainCategory
import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.model.label
import com.own.remindme.domain.repository.NotificationRepository
import com.own.remindme.domain.usecase.ReminderUseCases
import com.own.remindme.presentation.home.components.ReminderUiModel
import com.own.remindme.ui.theme.*
import com.own.remindme.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reminderUseCases: ReminderUseCases,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeReminders()
        observeUserPreferences()
        observeNotifications()
    }

    private fun observeNotifications() {
        notificationRepository.getUnreadCount()
            .onEach { count ->
                _uiState.update { it.copy(unreadNotificationsCount = count) }
            }.launchIn(viewModelScope)
    }

    private fun observeUserPreferences() {
        userPreferencesRepository.userPreferencesFlow
            .onEach { preferences ->
                val firstName = preferences.userName.trim().split("\\s+".toRegex()).first().ifBlank { "User" }
                _uiState.update { 
                    it.copy(greeting = "${greeting()}, $firstName")
                }
            }.launchIn(viewModelScope)
    }

    private fun observeReminders() {
        combine(
            reminderUseCases.getAllReminders(),
            _uiState.map { it.selectedCategory }.distinctUntilChanged(),
            _uiState.map { it.search }.distinctUntilChanged()
        ) { reminders, selectedId, searchQuery ->
            var filtered = if (selectedId == 0) {
                reminders
            } else {
                reminders.filter { it.category == getDomainCategory(selectedId) }
            }

            if (searchQuery.isNotBlank()) {
                filtered = filtered.filter { 
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
                }
            }
            
            filtered
        }.onEach { reminders ->

            val now = System.currentTimeMillis()
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val todayEnd = todayStart + 24 * 60 * 60 * 1000

            // Today section includes:
            // 1. Everything scheduled for today (past or future today)
            // 2. Everything from the past that is NOT completed (Overdue)
            val today = reminders.filter { 
                (it.reminderTime in todayStart until todayEnd) || 
                (it.reminderTime < todayStart && !it.completed) 
            }
            
            // Upcoming section includes:
            // 1. Everything scheduled for tomorrow onwards
            val upcoming = reminders.filter { it.reminderTime >= todayEnd }

            val uiToday = today.map { it.toUiModel() }
            val uiUpcoming = upcoming.map { it.toUiModel() }

            _uiState.update {
                it.copy(
                    categories = categories(),
                    todayReminders = uiToday,
                    upcomingReminders = uiUpcoming
                )
            }
        }.launchIn(viewModelScope)
    }

    fun refresh() {

        _uiState.update {
            it.copy(isRefreshing = true)
        }

        viewModelScope.launch {

            delay(1000)

            _uiState.update {
                it.copy(isRefreshing = false)
            }
        }
    }

    fun onCategoryClick(categoryId: Int) {
        _uiState.update {
            it.copy(selectedCategory = categoryId)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(search = query)
        }
    }

    fun toggleTaken(uiModel: ReminderUiModel) {
        viewModelScope.launch {
            reminderUseCases.getReminder(uiModel.id.toLong())?.let { reminder ->
                val newTimestamp = if (uiModel.isTakenToday) null else System.currentTimeMillis()
                reminderUseCases.updateReminder(reminder.copy(lastTakenTimestamp = newTimestamp))
            }
        }
    }

    fun deleteReminder(uiModel: ReminderUiModel) {
        viewModelScope.launch {
            reminderUseCases.getReminder(uiModel.id.toLong())?.let { reminder ->
                reminderUseCases.deleteReminder(reminder)
            }
        }
    }

    private fun greeting(): String {

        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {

            in 0..11 -> "Good Morning"

            in 12..16 -> "Good Afternoon"

            else -> "Good Evening"
        }
    }

    private fun categories() = listOf(

        CategoryUiModel(
            0,
            "All",
            AppIcons.GridView,
            Primary
        ),

        CategoryUiModel(
            1,
            "Medicine",
            AppIcons.Medication,
            MedicineColor
        ),

        CategoryUiModel(
            2,
            "Vehicle",
            AppIcons.DirectionsCar,
            VehicleColor
        ),

        CategoryUiModel(
            3,
            "Bills",
            AppIcons.Payments,
            BillsColor
        ),

        CategoryUiModel(
            4,
            "Documents",
            AppIcons.Description,
            DocumentColor
        ),

        CategoryUiModel(
            5,
            "Health",
            AppIcons.Health,
            HealthColor
        ),

        CategoryUiModel(
            6,
            "Birthday",
            AppIcons.Birthday,
            BirthdayColor
        ),

        CategoryUiModel(
            7,
            "Shopping",
            AppIcons.Shopping,
            ShoppingColor
        ),

        CategoryUiModel(
            8,
            "Custom",
            AppIcons.Calendar,
            Primary
        )
    )

    private fun getDomainCategory(id: Int): DomainCategory? {
        return when (id) {
            1 -> DomainCategory.MEDICINE
            2 -> DomainCategory.VEHICLE
            3 -> DomainCategory.BILL
            4 -> DomainCategory.DOCUMENT
            5 -> DomainCategory.HEALTH
            6 -> DomainCategory.BIRTHDAY
            7 -> DomainCategory.SHOPPING
            8 -> DomainCategory.CUSTOM
            else -> null
        }
    }

    private fun Reminder.toUiModel(): ReminderUiModel {
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val dateFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        val expiryFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + 24 * 60 * 60 * 1000

        val displayDate = when {
            reminderTime in todayStart until todayEnd -> timeFormat.format(java.util.Date(reminderTime))
            reminderTime < todayStart -> "Overdue: ${dateFormat.format(java.util.Date(reminderTime))}"
            else -> dateFormat.format(java.util.Date(reminderTime)) + ", " + timeFormat.format(java.util.Date(reminderTime))
        }

        return ReminderUiModel(

            id = id.toInt(),

            title = title,

            category = category.name,

            date = displayDate,

            repeat = repeatType.label,

            color = when (category) {

                DomainCategory.MEDICINE -> MedicineColor

                DomainCategory.VEHICLE -> VehicleColor

                DomainCategory.BILL -> BillsColor

                DomainCategory.DOCUMENT -> DocumentColor

                DomainCategory.HEALTH -> HealthColor

                DomainCategory.BIRTHDAY -> BirthdayColor

                DomainCategory.SHOPPING -> ShoppingColor

                else -> Primary
            },

            completed = completed,

            icon = when (category) {
                DomainCategory.MEDICINE -> AppIcons.Medication
                DomainCategory.VEHICLE -> AppIcons.DirectionsCar
                DomainCategory.BILL -> AppIcons.Payments
                DomainCategory.DOCUMENT -> AppIcons.Description
                DomainCategory.HEALTH -> AppIcons.Health
                DomainCategory.BIRTHDAY -> AppIcons.Birthday
                DomainCategory.SHOPPING -> AppIcons.Shopping
                else -> AppIcons.Calendar
            },

            expiryDate = expiryDate?.let { expiryFormat.format(java.util.Date(it)) },

            isMedicine = category == DomainCategory.MEDICINE,

            isTakenToday = lastTakenTimestamp?.let { 
                val cal1 = Calendar.getInstance().apply { timeInMillis = it }
                val cal2 = Calendar.getInstance()
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
            } ?: false
        )
    }
}

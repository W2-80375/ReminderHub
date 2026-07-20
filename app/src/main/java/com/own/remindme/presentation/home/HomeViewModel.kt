package com.own.remindme.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.own.remindme.domain.model.Category as DomainCategory
import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.usecase.ReminderUseCases
import com.own.remindme.presentation.home.components.ReminderUiModel
import com.own.remindme.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reminderUseCases: ReminderUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeReminders()
    }

    private fun observeReminders() {

        combine(
            reminderUseCases.getAllReminders(),
            _uiState.map { it.selectedCategory }.distinctUntilChanged()
        ) { reminders, selectedId ->

            val filtered = if (selectedId == 0) {
                reminders
            } else {
                reminders.filter { it.category == getDomainCategory(selectedId) }
            }

            filtered

        }.onEach { reminders ->

            val uiReminders = reminders.map { it.toUiModel() }

            _uiState.update {
                it.copy(
                    greeting = greeting(),
                    categories = categories(),
                    todayReminders = uiReminders,
                    upcomingReminders = emptyList() // We'll split later
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

    private fun greeting(): String {

        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {

            in 0..11 -> "Good Morning ☀"

            in 12..16 -> "Good Afternoon 🌤"

            else -> "Good Evening 🌙"
        }
    }

    private fun categories() = listOf(

        Category(
            0,
            "All",
            AppIcons.GridView,
            Primary
        ),

        Category(
            1,
            "Medicine",
            AppIcons.Medication,
            MedicineColor
        ),

        Category(
            2,
            "Vehicle",
            AppIcons.DirectionsCar,
            VehicleColor
        ),

        Category(
            3,
            "Bills",
            AppIcons.Payments,
            BillsColor
        ),

        Category(
            4,
            "Documents",
            AppIcons.Description,
            DocumentColor
        ),

        Category(
            5,
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
            5 -> DomainCategory.CUSTOM
            else -> null
        }
    }

    private fun Reminder.toUiModel(): ReminderUiModel {

        return ReminderUiModel(

            id = id.toInt(),

            title = title,

            category = category.name,

            date = reminderTime.toString(),

            repeat = repeatType.name,

            color = when (category) {

                DomainCategory.MEDICINE -> MedicineColor

                DomainCategory.VEHICLE -> VehicleColor

                DomainCategory.BILL -> BillsColor

                DomainCategory.DOCUMENT -> DocumentColor

                else -> Primary
            },

            completed = completed
        )
    }
}

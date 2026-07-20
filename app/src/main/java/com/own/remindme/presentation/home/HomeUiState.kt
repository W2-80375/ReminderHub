package com.own.remindme.presentation.home

import com.own.remindme.presentation.home.components.ReminderUiModel

data class HomeUiState(

    val greeting: String = "",

    val search: String = "",

    val categories: List<Category> = emptyList(),

    val selectedCategory: Int = 0,

    val todayReminders: List<ReminderUiModel> = emptyList(),

    val upcomingReminders: List<ReminderUiModel> = emptyList(),

    val isRefreshing: Boolean = false,

    val isLoading: Boolean = false

) {
    val todayCount: Int
        get() = todayReminders.size

    val upcomingCount: Int
        get() = upcomingReminders.size
}
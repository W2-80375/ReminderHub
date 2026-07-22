package com.own.remindme.presentation.navigation


sealed class Screen(val route: String) {

    data object Splash : Screen("splash")

    data object Onboarding : Screen("onboarding")

    data object Home : Screen("home")

    data object AddReminder : Screen("add")

    data object ReminderDetail : Screen("detail")

    data object Notifications : Screen("notifications")

    data object Settings : Screen("settings")

    data object Statistics : Screen("statistics")
}
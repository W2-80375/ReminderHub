package com.own.remindme.presentation.home.components

import androidx.compose.ui.graphics.Color
import com.own.remindme.ui.theme.AppIcon

data class ReminderUiModel(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val date: String,
    val repeat: String,
    val startDate: String,
    val color: Color,
    val completed: Boolean,
    val icon: AppIcon? = null,
    val expiryDate: String? = null,
    val isExpired: Boolean = false,
    val isMedicine: Boolean = false,
    val isTakenToday: Boolean = false
)

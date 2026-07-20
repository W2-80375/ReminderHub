package com.own.remindme.presentation.home.components

import androidx.compose.ui.graphics.Color

data class ReminderUiModel(

    val id: Int,

    val title: String,

    val category: String,

    val date: String,

    val repeat: String,

    val color: Color,

    val completed: Boolean
)
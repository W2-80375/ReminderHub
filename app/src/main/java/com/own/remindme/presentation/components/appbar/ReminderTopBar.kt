package com.own.remindme.presentation.components.appbar

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTopBar(

    title: String

) {

    CenterAlignedTopAppBar(

        title = {

            Text(title)

        }

    )
}
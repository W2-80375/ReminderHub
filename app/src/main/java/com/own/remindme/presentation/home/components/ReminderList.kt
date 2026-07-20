package com.own.remindme.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun ReminderList(

    reminders: List<ReminderUiModel>

) {

    LazyColumn(

        verticalArrangement = Arrangement.spacedBy(12.dp)

    ) {

        items(reminders) {

            ReminderItem(

                reminder = it

            )

        }

    }

}
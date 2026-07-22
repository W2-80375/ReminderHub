package com.own.remindme.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.own.remindme.ui.theme.ReminderTheme

@Preview(
    showBackground = true
)
@Composable
fun HomePreview() {

    ReminderTheme {

        HomeScreen(
            onAddReminderClick = {},
            onReminderClick = {},
            onEditReminderClick = {},
            onNotificationClick = {},
            onSettingsClick = {}
        )

    }

}
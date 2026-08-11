package com.own.remindme.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.own.remindme.presentation.detail.ReminderDetailContent
import com.own.remindme.ui.theme.DarkBgEnd
import com.own.remindme.ui.theme.LocalDarkTheme

@Composable
fun AdaptiveHomeScreen(
    windowSizeClass: WindowSizeClass,
    onAddReminderClick: () -> Unit,
    onReminderClick: (Long) -> Unit,
    onEditReminderClick: (Long) -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val isDark = LocalDarkTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBgEnd else Color.White)
    ) {
        if (isExpanded) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    HomeScreen(
                        viewModel = viewModel,
                        onAddReminderClick = onAddReminderClick,
                        onReminderClick = { id ->
                            viewModel.onReminderSelected(id)
                        },
                        onEditReminderClick = onEditReminderClick,
                        onNotificationClick = onNotificationClick,
                        onSettingsClick = onSettingsClick
                    )
                }
                
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Box(modifier = Modifier.weight(1.2f)) {
                    if (state.selectedReminder != null) {
                        ReminderDetailContent(
                            reminder = state.selectedReminder,
                            showTopBar = true,
                            onBackClick = { viewModel.onReminderSelected(null) }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select a reminder to see details",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        } else {
            HomeScreen(
                viewModel = viewModel,
                onAddReminderClick = onAddReminderClick,
                onReminderClick = onReminderClick,
                onEditReminderClick = onEditReminderClick,
                onNotificationClick = onNotificationClick,
                onSettingsClick = onSettingsClick
            )
        }
    }
}

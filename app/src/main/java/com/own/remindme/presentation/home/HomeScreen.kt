package com.own.remindme.presentation.home


import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.own.remindme.presentation.components.common.SectionHeader
import com.own.remindme.presentation.components.empty.EmptyState
import com.own.remindme.presentation.components.fab.AnimatedFab
import com.own.remindme.presentation.home.components.CategoryRow
import com.own.remindme.presentation.home.components.GreetingSection
import com.own.remindme.presentation.home.components.HomeSearchBar
import com.own.remindme.presentation.home.components.ReminderItem
import com.own.remindme.presentation.home.components.SummaryCard
import com.own.remindme.ui.theme.*

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddReminderClick: () -> Unit,
    onReminderClick: (Long) -> Unit,
    onEditReminderClick: (Long) -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit
) {

    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {

            AnimatedFab(
                expanded = true,
                onClick = onAddReminderClick
            )

        }

    ) { padding ->

        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = state.isRefreshing,
            onRefresh = {
                viewModel.refresh()
            }
        ) {
            val isDark = LocalDarkTheme.current
            val onSurface = MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) DarkBgEnd else Color.White)
            ) {
                if (isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(MeshColor1.copy(alpha = 0.6f), Color.Transparent),
                                    center = Offset(0f, 0f),
                                    radius = 800f
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(MeshColor2.copy(alpha = 0.4f), Color.Transparent),
                                    center = Offset(1000f, 500f),
                                    radius = 1000f
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(MeshColor3.copy(alpha = 0.5f), Color.Transparent),
                                    center = Offset(0f, 1500f),
                                    radius = 1200f
                                )
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                GreetingSection(
                    greeting = state.greeting,
                    unreadCount = state.unreadNotificationsCount,
                    onNotificationClick = {
                        onNotificationClick()
                    },
                    onSettingsClick = {
                        onSettingsClick()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Today",
                        count = state.todayReminders.size,
                        gradient = SummaryTodayGradient,
                    )

                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Upcoming",
                        count = state.upcomingReminders.size,
                        gradient = SummaryUpcomingGradient,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HomeSearchBar(
                    value = state.search,
                    onValueChange = {
                        viewModel.onSearchQueryChange(it)
                    }
                )

                CategoryRow(
                    categories = state.categories,
                    selected = state.selectedCategory,
                    onCategoryClick = {
                        viewModel.onCategoryClick(it)
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
                    contentPadding = PaddingValues(
                        bottom = 150.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (state.todayReminders.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Today's Reminders",
                                titleColor = onSurface
                            )
                        }

                        items(state.todayReminders) {
                            ReminderItem(
                                reminder = it,
                                onClick = { onReminderClick(it.id.toLong()) },
                                onEditClick = { onEditReminderClick(it.id.toLong()) },
                                onDeleteClick = {
                                    viewModel.deleteReminder(it)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Reminder deleted")
                                    }
                                },
                                onTakenClick = {
                                    viewModel.toggleTaken(it)
                                },
                                showStatusBadge = true
                            )
                        }
                    } else if (state.upcomingReminders.isEmpty()) {
                        item {
                            SectionHeader(
                                title = "Today's Reminders",
                                titleColor = onSurface
                            )
                        }
                        item {
                            EmptyState()
                        }
                    }

                    if (state.upcomingReminders.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Upcoming",
                                titleColor = onSurface
                            )
                        }

                        items(state.upcomingReminders) {
                            ReminderItem(
                                reminder = it,
                                onClick = { onReminderClick(it.id.toLong()) },
                                onEditClick = { onEditReminderClick(it.id.toLong()) },
                                onDeleteClick = {
                                    viewModel.deleteReminder(it)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Reminder deleted")
                                    }
                                },
                                onTakenClick = {
                                    viewModel.toggleTaken(it)
                                }
                            )
                        }
                    }
                }
            }

        }

    }

}
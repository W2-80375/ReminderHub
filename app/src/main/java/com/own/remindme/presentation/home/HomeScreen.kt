package com.own.remindme.presentation.home


import androidx.compose.foundation.background
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

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddReminderClick: () -> Unit
) {

    val state by viewModel.uiState.collectAsState()

    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(

        containerColor = Color.Transparent,

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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBgEnd)
            ) {
                // Mesh Gradient Effect
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

            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentPadding = PaddingValues(20.dp),

                verticalArrangement = Arrangement.spacedBy(20.dp)

            ) {

                item {

                    GreetingSection(
                        greeting = state.greeting
                    )

                }

                item {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Today",
                            count = state.todayReminders.size,
                            gradient = SummaryTodayGradient
                        )

                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Upcoming",
                            count = state.upcomingReminders.size,
                            gradient = SummaryUpcomingGradient
                        )

                    }

                }

                item {

                    HomeSearchBar(

                        value = state.search,

                        onValueChange = {}

                    )

                }

                item {

                    CategoryRow(

                        categories = state.categories,

                        selected = state.selectedCategory,

                        onCategoryClick = {
                            viewModel.onCategoryClick(it)
                        }

                    )

                }

                item {

                    SectionHeader(
                        title = "Today's Reminders"
                    )

                }

                if (state.todayReminders.isEmpty()) {

                    item {

                        EmptyState()

                    }

                } else {

                    items(state.todayReminders) {

                        ReminderItem(reminder = it)

                    }

                }

                item {

                    SectionHeader(
                        title = "Upcoming"
                    )

                }

                items(state.upcomingReminders) {

                    ReminderItem(reminder = it)

                }

            }

        }

    }

}
package com.own.remindme.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.own.remindme.presentation.add_reminder.AddReminderScreen
import com.own.remindme.presentation.detail.ReminderDetailScreen
import com.own.remindme.presentation.home.HomeScreen
import com.own.remindme.presentation.notifications.NotificationsScreen
import com.own.remindme.presentation.onboarding.OnboardingScreen
import com.own.remindme.presentation.settings.SettingsScreen
import com.own.remindme.presentation.splash.SplashScreen


import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(

        navController = navController,

        startDestination = Screen.Splash.route

    ) {

        composable(Screen.Splash.route) {

            SplashScreen {

                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) {
                        inclusive = true
                    }
                }

            }

        }

        composable(Screen.Onboarding.route) {

            OnboardingScreen {

                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) {
                        inclusive = true
                    }
                }

            }

        }

        composable(Screen.Home.route) {

            HomeScreen(
                onAddReminderClick = {
                    navController.navigate(Screen.AddReminder.route + "?reminderId=-1")
                },
                onReminderClick = { id ->
                    navController.navigate(Screen.ReminderDetail.route + "?reminderId=$id")
                },
                onEditReminderClick = { id ->
                    navController.navigate(Screen.AddReminder.route + "?reminderId=$id")
                },
                onNotificationClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )

        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBackClick = { navController.navigateUp() })
        }

        composable(
            route = Screen.AddReminder.route + "?reminderId={reminderId}",
            arguments = listOf(
                navArgument("reminderId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddReminderScreen(navController = navController)
        }

        composable(
            route = Screen.ReminderDetail.route + "?reminderId={reminderId}",
            arguments = listOf(
                navArgument("reminderId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            ReminderDetailScreen(navController = navController)
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onBackClick = { navController.navigateUp() })
        }

    }

}
package com.own.remindme.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.own.remindme.presentation.add_reminder.AddReminderScreen
import com.own.remindme.presentation.home.HomeScreen
import com.own.remindme.presentation.onboarding.OnboardingScreen
import com.own.remindme.presentation.splash.SplashScreen


@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(

        navController = navController,

        startDestination = Screen.Splash.route

    ) {

        composable(Screen.Splash.route) {

            SplashScreen {

                navController.navigate(Screen.Onboarding.route) {
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
                    navController.navigate(Screen.AddReminder.route)
                }
            )

        }

        composable(Screen.AddReminder.route) {
            AddReminderScreen(navController = navController)
        }

    }

}
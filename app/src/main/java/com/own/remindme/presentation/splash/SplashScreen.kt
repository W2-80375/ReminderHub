package com.own.remindme.presentation.splash


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(

    navigate: () -> Unit

) {

    LaunchedEffect(Unit) {

        delay(2000)

        navigate()

    }

    Box(

        modifier = Modifier.fillMaxSize(),

        contentAlignment = Alignment.Center

    ) {

        Text(

            text = "ReminderHub",

            fontSize = 30.sp

        )

    }

}
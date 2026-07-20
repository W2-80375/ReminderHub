package com.own.remindme.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(

    onContinue: () -> Unit

) {

    Box(

        modifier = Modifier.fillMaxSize(),

        contentAlignment = Alignment.Center

    ) {

        Column(

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Text(

                text = "Never Miss Anything"

            )

            Spacer(

                modifier = Modifier.height(16.dp)

            )

            Button(

                onClick = onContinue

            ) {

                Text("Get Started")

            }

        }

    }

}
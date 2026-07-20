package com.own.remindme.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.own.remindme.ui.theme.DarkText
import com.own.remindme.ui.theme.Dimens

@Composable
fun GreetingSection(

    greeting: String

) {

    Column(

        modifier = Modifier.fillMaxWidth()

    ) {

        Text(

            text = greeting,

            style = MaterialTheme.typography.headlineMedium,

            color = DarkText

        )

        Spacer(

            modifier = Modifier.height(Dimens.Space8)

        )

        Text(

            text = "Manage all your reminders effortlessly",

            style = MaterialTheme.typography.bodyMedium,

            color = DarkText.copy(alpha = 0.6f)

        )

    }

}
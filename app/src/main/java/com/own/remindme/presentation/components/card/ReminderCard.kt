package com.own.remindme.presentation.components.card

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReminderCard(

    title: String,

    subtitle: String,

    repeat: String,

    onClick: () -> Unit

) {

    ElevatedCard(

        modifier = Modifier.fillMaxWidth(),

        onClick = onClick

    ) {

        Column(

            modifier = Modifier.padding(16.dp)

        ) {

            Text(

                title,

                style = MaterialTheme.typography.titleMedium

            )

            Spacer(Modifier.height(4.dp))

            Text(subtitle)

            Spacer(Modifier.height(8.dp))

            Text(

                repeat,

                color = MaterialTheme.colorScheme.primary

            )

        }

    }
}
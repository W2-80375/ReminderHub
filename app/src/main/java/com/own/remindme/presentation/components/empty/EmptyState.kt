package com.own.remindme.presentation.components.empty

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(

    modifier: Modifier = Modifier

) {

    Column(

        modifier = modifier.fillMaxWidth(),

        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Icon(

            imageVector = Icons.Default.EventBusy,

            contentDescription = null,

            modifier = Modifier.size(72.dp),

            tint = MaterialTheme.colorScheme.primary

        )

        Spacer(

            Modifier.height(16.dp)

        )

        Text(

            "Nothing to remind today",

            style = MaterialTheme.typography.titleMedium

        )

        Spacer(

            Modifier.height(8.dp)

        )

        Text(

            "Tap the + button to create your first reminder."

        )

    }

}
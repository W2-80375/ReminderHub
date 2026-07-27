package com.own.remindme.presentation.components.empty

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.EventBusy,
            contentDescription = "No reminders",
            modifier = Modifier.size(56.dp),
            tint = onSurface.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Nothing to remind today",
            color = onSurface,
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tap the + button to create a new reminder.",
            color = onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
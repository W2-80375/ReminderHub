package com.own.remindme.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.own.remindme.ui.theme.*

@Composable
fun GreetingSection(
    greeting: String,
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalDarkTheme.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    )
    {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleLarge,
                color = onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Manage all your reminders effortlessly",
                style = MaterialTheme.typography.bodySmall,
                color = onSurface.copy(alpha = 0.6f)
            )
        }

        Row {
            IconButton(onClick = onNotificationClick) {
                Box {
                    AppIcon(
                        icon = AppIcons.Notifications,
                        contentDescription = "Notifications",
                        tint = if (!isDark) DarkBackground else onSurface
                    )
                    
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(Color.Red),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onSettingsClick) {
                AppIcon(
                    icon = AppIcons.Settings,
                    contentDescription = "Settings",
                    tint = if (!isDark) DarkBackground else onSurface
                )
            }
        }
    }
}

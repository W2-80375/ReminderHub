package com.own.remindme.presentation.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.own.remindme.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalDarkTheme.current

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = onSurface,
                    titleContentColor = onSurface,
                    actionIconContentColor = onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.deleteAllNotifications() }) {
                            Text(
                                "Delete all",
                                style = TextStyle(
                                    color = Color.Red.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) 
                        Brush.verticalGradient(colors = listOf(DarkBgStart, DarkBgEnd))
                    else
                        Brush.verticalGradient(colors = listOf(Color.White, Color.White))
                )
        )

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(GradientPurple), alpha = 0.1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Primary.copy(alpha = 0.4f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("No notifications yet", color = onSurface, fontWeight = FontWeight.Medium)
                    Text("We'll alert you here", color = onSurface.copy(alpha = 0.4f), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = { viewModel.markAsRead(notification.id) },
                        onDelete = { viewModel.deleteNotification(notification) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: com.own.remindme.data.local.NotificationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val onSurface = MaterialTheme.colorScheme.onSurface
    val isDark = LocalDarkTheme.current
    val cardColor = if (isDark) {
        if (notification.isRead) DarkCard.copy(alpha = 0.4f) else DarkCard
    } else {
        if (notification.isRead) Color(0xFFF8F9FA) else Color.White
    }

    val isExpiry = notification.title.startsWith("Medicine Expiry Alert")
    val displayTitle = if (notification.title.startsWith("Reminder: ")) {
        "Reminder for \"${notification.title.removePrefix("Reminder: ")}\""
    } else if (isExpiry) {
        "Expiry Reminder for \"${notification.message.substringAfter("'").substringBefore("'")}\""
    } else {
        notification.title
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .graphicsLayer {
                shadowElevation = 4f
                shape = RoundedCornerShape(16.dp)
                clip = true
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = if (!notification.isRead) 
            BorderStroke(0.8.dp, Brush.linearGradient(GradientPurple)) 
        else if (!isDark)
            BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
        else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .then(
                        if (notification.isRead) {
                            Modifier.background(onSurface.copy(alpha = 0.05f))
                        } else {
                            Modifier.background(Brush.linearGradient(GradientPurple))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpiry) Icons.Default.EventBusy else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (notification.isRead) onSurface.copy(alpha = 0.3f) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayTitle,
                    color = onSurface,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                    fontSize = 15.sp
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = if (isExpiry) "Expiry Reminder" else notification.category.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (isExpiry) Color.Red else Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(2.dp).clip(CircleShape).background(onSurface.copy(alpha = 0.2f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sdf.format(Date(notification.timestamp)),
                        color = onSurface.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Delete", 
                    tint = Color.Red.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

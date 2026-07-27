package com.own.remindme.presentation.home.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.own.remindme.ui.theme.*

@Composable
fun ReminderItem(
    reminder: ReminderUiModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onTakenClick: () -> Unit = {},
    showStatusBadge: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDark = LocalDarkTheme.current
    
    val cardColor = if (isDark) DarkCard else Color.White
    val textColor = if (isDark) DarkText else TextPrimary
    val subTextColor = if (isDark) DarkText.copy(alpha = 0.6f) else TextSecondary

    val gradient = when (reminder.color) {
        com.own.remindme.ui.theme.MedicineColor -> com.own.remindme.ui.theme.GradientAmber
        com.own.remindme.ui.theme.VehicleColor -> com.own.remindme.ui.theme.GradientBlue
        com.own.remindme.ui.theme.BillsColor -> com.own.remindme.ui.theme.GradientRed
        com.own.remindme.ui.theme.DocumentColor -> com.own.remindme.ui.theme.GradientPurple
        else -> listOf(Primary, Primary.copy(alpha = 0.8f))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .graphicsLayer {
                rotationX = 2f
                rotationY = -1f
                cameraDistance = 16f * density
            }
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = 0.8.dp,
                brush = Brush.linearGradient(
                    colors = if (isDark) 
                        listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                    else
                        listOf(Color.Black.copy(alpha = 0.05f), Color.Transparent)
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Taken/Pending Badge
        if (reminder.isMedicine && showStatusBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(bottomStart = 12.dp))
                    .background(if (reminder.isTakenToday) com.own.remindme.ui.theme.Success else Color(0xFFFFA500))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (reminder.isTakenToday) "Taken" else "Pending",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D sphere indicator look
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                            center = Offset(8f, 8f)
                        )
                    )
                    .background(Brush.linearGradient(colors = gradient))
            ) {
                reminder.icon?.let {
                    AppIcon(
                        icon = it,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = reminder.startDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor,
                    fontSize = 10.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${reminder.date}, ${reminder.repeat}",
                        color = subTextColor.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }

                if (reminder.isExpired) {
                    Text(
                        text = "Expired",
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    reminder.expiryDate?.let {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(2.dp).clip(CircleShape).background(subTextColor.copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Exp: $it",
                            color = com.own.remindme.ui.theme.MedicineColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (reminder.completed) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = com.own.remindme.ui.theme.Success,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    AppIcon(
                        icon = AppIcons.More,
                        contentDescription = "More",
                        tint = textColor.copy(alpha = 0.6f)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(cardColor)
                ) {
                    if (reminder.isMedicine) {
                        DropdownMenuItem(
                            text = { Text(if (reminder.isTakenToday) "Mark as Pending" else "Mark as Taken", color = textColor) },
                            leadingIcon = { Icon(if (reminder.isTakenToday) Icons.Default.Schedule else Icons.Default.CheckCircle, contentDescription = null, tint = if (reminder.isTakenToday) textColor else com.own.remindme.ui.theme.Success) },
                            onClick = {
                                showMenu = false
                                onTakenClick()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Edit", color = textColor) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Primary) },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

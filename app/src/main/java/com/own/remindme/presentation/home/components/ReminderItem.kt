package com.own.remindme.presentation.home.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.own.remindme.ui.theme.DarkCard
import com.own.remindme.ui.theme.DarkText
import com.own.remindme.ui.theme.Primary

@Composable
fun ReminderItem(
    reminder: ReminderUiModel
) {
    val gradient = when (reminder.color) {
        com.own.remindme.ui.theme.MedicineColor -> com.own.remindme.ui.theme.GradientRed
        com.own.remindme.ui.theme.VehicleColor -> com.own.remindme.ui.theme.GradientBlue
        com.own.remindme.ui.theme.BillsColor -> com.own.remindme.ui.theme.GradientAmber
        com.own.remindme.ui.theme.DocumentColor -> com.own.remindme.ui.theme.GradientPurple
        else -> listOf(Primary, Primary.copy(alpha = 0.8f))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationX = 2f
                rotationY = -1f
                cameraDistance = 16f * density
            }
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(
                width = 0.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D sphere indicator look
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                            center = Offset(4f, 4f)
                        )
                    )
                    .background(Brush.linearGradient(colors = gradient))
            )

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkText,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reminder.date,
                        color = DarkText.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Box(modifier = Modifier.size(2.dp).clip(CircleShape).background(DarkText.copy(alpha = 0.3f)))
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = reminder.repeat,
                        color = gradient.first(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (reminder.completed) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = com.own.remindme.ui.theme.Success,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

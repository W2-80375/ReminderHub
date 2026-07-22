package com.own.remindme.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.own.remindme.ui.theme.AppIcons

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    time: String? = null,
    frequency: String? = null,
    expiryDate: String? = null
) {
    Box(
        modifier = modifier
    ) {
        // Drop Shadow Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .offset(y = 6.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(gradient[0].copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                gradient[0],
                                gradient[1].copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                // Top Right Icons
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = count.toString(),
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(4f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                }
            }
        }
    }
}

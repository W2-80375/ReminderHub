package com.own.remindme.presentation.components.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.own.remindme.ui.theme.*

@Composable
fun CategoryChip(
    title: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isDark = LocalDarkTheme.current
    val cardColor = if (isDark) DarkCard else Color(0xFFEEEEEE)
    val textColor = if (isDark) DarkText else TextPrimary

    val gradient = when (color) {
        MedicineColor -> GradientAmber
        VehicleColor -> GradientBlue
        BillsColor -> GradientRed
        DocumentColor -> GradientPurple
        else -> listOf(Primary, Primary.copy(alpha = 0.8f))
    }

    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (selected) {
                    Modifier.background(Brush.linearGradient(colors = gradient))
                } else {
                    Modifier
                        .background(cardColor)
                        .border(
                            1.dp, 
                            if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f), 
                            RoundedCornerShape(16.dp)
                        )
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = if (selected) Color.White else textColor.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

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
import com.own.remindme.ui.theme.DarkCard
import com.own.remindme.ui.theme.DarkText
import com.own.remindme.ui.theme.Primary

@Composable
fun CategoryChip(
    title: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val gradient = when (color) {
        com.own.remindme.ui.theme.MedicineColor -> com.own.remindme.ui.theme.GradientAmber
        com.own.remindme.ui.theme.VehicleColor -> com.own.remindme.ui.theme.GradientBlue
        com.own.remindme.ui.theme.BillsColor -> com.own.remindme.ui.theme.GradientRed
        com.own.remindme.ui.theme.DocumentColor -> com.own.remindme.ui.theme.GradientPurple
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
                        .background(DarkCard)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = if (selected) Color.White else DarkText.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

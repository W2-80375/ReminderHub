package com.own.remindme.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.own.remindme.ui.theme.*

@Composable
fun HomeSearchBar(
    value: String,
    onValueChange: (String) -> Unit
) {
    val isDark = LocalDarkTheme.current
    val textColor = if (isDark) Color.White else TextPrimary
    val containerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)

    TextField(
        modifier = Modifier.fillMaxWidth().height(45.dp),
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = textColor, fontSize = 13.sp),
        placeholder = {
            Text(
                "Search reminders",
                color = textColor.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            AppIcon(
                icon = AppIcons.Search,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = textColor,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

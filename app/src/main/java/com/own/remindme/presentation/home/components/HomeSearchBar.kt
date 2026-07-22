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
import com.own.remindme.ui.theme.AppIcon
import com.own.remindme.ui.theme.AppIcons

@Composable
fun HomeSearchBar(
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
        placeholder = {
            Text(
                "Search reminders",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            AppIcon(
                icon = AppIcons.Search,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

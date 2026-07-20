package com.own.remindme.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(

    primary = Primary,

    secondary = Secondary,

    background = Background,

    surface = Surface,

    error = Error
)

private val DarkScheme = darkColorScheme(

    primary = DarkPrimary,

    background = DarkBackground,

    surface = DarkSurface,

    error = Error
)

@Composable
fun ReminderTheme(

    darkTheme: Boolean = isSystemInDarkTheme(),

    content: @Composable () -> Unit

) {

    MaterialTheme(

        colorScheme = if (darkTheme) DarkScheme else LightScheme,

        typography = AppTypography,

        shapes = AppShapes,

        content = content
    )
}
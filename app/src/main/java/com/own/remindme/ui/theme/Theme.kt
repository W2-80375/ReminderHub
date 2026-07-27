package com.own.remindme.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalDarkTheme = compositionLocalOf { false }

private val LightScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = Color(0xFF006B4F),
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color.Black.copy(alpha = 0.6f),
    error = Error,
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF3E43D9),
    onPrimaryContainer = Color.White,
    secondary = Secondary,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkText.copy(alpha = 0.6f),
    error = Error,
    onError = Color.White
)

@Composable
fun ReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

package com.tradingdashboard.presentation.theme

import androidx.compose.foundation.isSystemInDarkMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Colors matching website theme
val PrimaryGreen = Color(0xFF10B981)
val PrimaryGreen80 = Color(0xFF34D399)
val PrimaryDark = Color(0xFF0F172A)
val PrimaryDarkBg = Color(0xFF1E293B)
val CardDarkBg = Color(0xFF334155)
val TextMuted = Color(0xFF94A3B8)
val TextLight = Color(0xFFE2E8F0)
val RedLoss = Color(0xFFEF4444)
val GreenWin = Color(0xFF10B981)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = PrimaryDark,
    secondary = PrimaryGreen80,
    onSecondary = PrimaryDark,
    background = PrimaryDark,
    onBackground = TextLight,
    surface = CardDarkBg,
    onSurface = TextLight,
    error = RedLoss,
    onError = Color.White
)

val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    secondary = PrimaryGreen80,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = PrimaryDark,
    surface = Color(0xFFF8FAFC),
    onSurface = PrimaryDark,
    error = RedLoss,
    onError = Color.White
)

@Composable
fun TradingDashboardTheme(
    darkTheme: Boolean = isSystemInDarkMode(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Local composition for theme state
val LocalThemeState = staticCompositionLocalOf<ThemeState> {
    error("CompositionLocal LocalThemeState not provided")
}

data class ThemeState(
    val isDarkMode: Boolean = true,
    val currency: String = "USD"
)

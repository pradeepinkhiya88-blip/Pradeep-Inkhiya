package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AutoClipPrimary,
    onPrimary = Color.White,
    primaryContainer = AutoClipPrimaryContainer,
    onPrimaryContainer = AutoClipOnPrimaryContainer,
    secondary = AutoClipSecondary,
    onSecondary = Color.White,
    secondaryContainer = AutoClipSecondaryContainer,
    onSecondaryContainer = AutoClipOnSecondaryContainer,
    tertiary = AutoClipTertiary,
    onTertiary = Color.Black,
    tertiaryContainer = AutoClipTertiaryContainer,
    onTertiaryContainer = AutoClipOnTertiaryContainer,
    background = DarkBackground,
    onBackground = Color(0xFFF0F1F5),
    surface = DarkSurface,
    onSurface = Color(0xFFF0F1F5),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCACEDB),
    outline = DarkBorder,
    outlineVariant = Color(0xFF383C52)
)

private val LightColorScheme = lightColorScheme(
    primary = AutoClipPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE5EB),
    onPrimaryContainer = AutoClipPrimaryDark,
    secondary = AutoClipSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE4FF),
    onSecondaryContainer = Color(0xFF4C249E),
    tertiary = Color(0xFF007A99),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF14151B),
    surface = LightSurface,
    onSurface = Color(0xFF14151B),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF49454F),
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode suited for video editors
    dynamicColor: Boolean = false, // Keep branded high-contrast theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

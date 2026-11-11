package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PawchiveCyan,
    onPrimary = Color(0xFF00262C),
    primaryContainer = PawchiveCyanSoft,
    onPrimaryContainer = Color(0xFFB8F8FF),
    secondary = JoyReactorAmber,
    onSecondary = Color(0xFF2B1C00),
    secondaryContainer = JoyReactorAmberSoft,
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = TagCopyrightColor,
    onTertiary = Color(0xFF280047),
    background = ObsidianBackground,
    onBackground = Color(0xFFECEFF8),
    surface = ObsidianSurface,
    onSurface = Color(0xFFECEFF8),
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B8D1),
    outline = Color(0xFF3A425E)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigoLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F1FF),
    onPrimaryContainer = Color(0xFF001F29),
    secondary = SecondaryAmberLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDF9E),
    onSecondaryContainer = Color(0xFF2B1B00),
    tertiary = Color(0xFF7B2CBF),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF141824),
    surface = LightSurface,
    onSurface = Color(0xFF141824),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF454D66),
    outline = Color(0xFFC2C9DF)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

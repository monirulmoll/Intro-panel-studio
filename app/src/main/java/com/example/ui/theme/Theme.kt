package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = StudioObsidian,
    primaryContainer = Color(0xFF003642),
    onPrimaryContainer = ElectricCyan,
    secondary = NeuralViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E1065),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = CompilerAmber,
    onTertiary = StudioObsidian,
    tertiaryContainer = Color(0xFF452700),
    onTertiaryContainer = Color(0xFFFFE082),
    background = StudioObsidian,
    onBackground = TextPrimaryDark,
    surface = StudioSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = StudioBorder,
    error = ErrorCrimson
)

private val StudioLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    secondary = LightSecondary,
    onSecondary = Color.White,
    tertiary = CompilerAmber,
    background = LightBackground,
    surface = LightSurface,
    onBackground = StudioObsidian,
    onSurface = StudioObsidian
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudioDarkColorScheme else StudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

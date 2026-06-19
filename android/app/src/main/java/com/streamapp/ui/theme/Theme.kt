package com.streamapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Background,
    primaryContainer = Primary.copy(alpha = 0.15f),
    onPrimaryContainer = Primary,
    secondary = Secondary,
    onSecondary = Background,
    secondaryContainer = Secondary.copy(alpha = 0.15f),
    onSecondaryContainer = Secondary,
    tertiary = AccentBlue,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = GlassBorder,
    error = LiveRed,
    onError = Background,
    inverseSurface = TextPrimary,
    inverseOnSurface = Background,
    surfaceTint = Primary,
    surfaceBright = Surface,
    surfaceDim = Background,
    surfaceContainer = SurfaceVariant,
    surfaceContainerHigh = Color(0xFF222238),
    surfaceContainerHighest = Color(0xFF2A2A40)
)

@Composable
fun StreamAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

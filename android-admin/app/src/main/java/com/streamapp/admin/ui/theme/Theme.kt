package com.streamapp.admin.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Background = Color(0xFF10141F)
val Surface = Color(0xFF181C28)
val Surface2 = Color(0xFF1E2335)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B8D0)
val TextTertiary = Color(0xFF6A72A0)
val Primary = Color(0xFF00E676)
val PrimaryDim = Color(0x2600E676)
val Secondary = Color(0xFF7C5CFC)
val LiveRed = Color(0xFFFF1744)
val Warning = Color(0xFFFFD600)
val GlassBorder = Color(0x1AFFFFFF)

private val DarkScheme = darkColorScheme(
    primary = Primary, onPrimary = Background, primaryContainer = PrimaryDim,
    secondary = Secondary, onSecondary = Background,
    background = Background, onBackground = TextPrimary,
    surface = Surface, onSurface = TextPrimary,
    surfaceVariant = Surface2, onSurfaceVariant = TextSecondary,
    outline = GlassBorder, error = LiveRed
)

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, content = content)
}

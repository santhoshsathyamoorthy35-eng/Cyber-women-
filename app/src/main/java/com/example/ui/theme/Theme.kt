package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CyberWomenDarkColorScheme = darkColorScheme(
    primary = ShieldCyan,
    onPrimary = DeepObsidian,
    primaryContainer = PanelNavy,
    onPrimaryContainer = TextPrimary,
    secondary = SafeEmerald,
    onSecondary = DeepObsidian,
    secondaryContainer = Color(0xFF0A2A1E),
    onSecondaryContainer = Color(0xFF9AF0C7),
    tertiary = CyberPurple,
    onTertiary = DeepObsidian,
    tertiaryContainer = Color(0xFF21173C),
    onTertiaryContainer = Color(0xFFE3D4FF),
    error = CriticalRose,
    onError = Color.White,
    errorContainer = Color(0xFF3A0C16),
    onErrorContainer = Color(0xFFFFC9D3),
    background = DeepObsidian,
    onBackground = TextPrimary,
    surface = CommandNavy,
    onSurface = TextPrimary,
    surfaceVariant = PanelNavy,
    onSurfaceVariant = TextMuted,
    outline = HairlineBorder,
    outlineVariant = HairlineBright
)

val CyberWomenShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CyberWomenTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberWomenDarkColorScheme,
        typography = Typography,
        shapes = CyberWomenShapes,
        content = content
    )
}

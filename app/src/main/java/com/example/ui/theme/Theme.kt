package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BeatBounceColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = PureWhite,
    secondary = ElectricCyan,
    onSecondary = DeepBlack,
    tertiary = NeonPink,
    onTertiary = PureWhite,
    background = DeepBlack,
    onBackground = PureWhite,
    surface = DarkViolet,
    onSurface = PureWhite,
    surfaceVariant = CardSurface,
    onSurfaceVariant = SoftLavender,
    outline = NeonPurple.copy(alpha = 0.5f)
)

@Composable
fun BeatBounceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BeatBounceColorScheme,
        typography = Typography,
        content = content
    )
}

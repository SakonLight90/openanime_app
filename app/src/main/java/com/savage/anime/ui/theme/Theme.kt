package com.savage.anime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val DarkColorScheme = darkColorScheme(
    primary = RedNetflix,
    secondary = White,
    background = Black,
    surface = DarkGray,
    onPrimary = White,
    onSecondary = Black,
    onBackground = White,
    onSurface = White
)

@Composable
fun AnimeAppTheme(
    accentColor: Color = RedNetflix,
    content: @Composable () -> Unit
) {
    val onPrimary = if (accentColor.luminance() > 0.5f) Black else White
    val colorScheme = DarkColorScheme.copy(primary = accentColor, onPrimary = onPrimary)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

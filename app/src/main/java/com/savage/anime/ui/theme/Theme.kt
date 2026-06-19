package com.savage.anime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    val colorScheme = DarkColorScheme.copy(primary = accentColor)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

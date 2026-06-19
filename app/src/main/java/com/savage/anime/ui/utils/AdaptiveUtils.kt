package com.savage.anime.ui.utils

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

enum class ScreenWidthClass { Compact, Medium, Expanded }

@Composable
fun currentScreenWidthClass(): ScreenWidthClass {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 600 -> ScreenWidthClass.Compact
        widthDp < 840 -> ScreenWidthClass.Medium
        else -> ScreenWidthClass.Expanded
    }
}

fun adaptiveGrid(minItemWidth: Dp): GridCells {
    return GridCells.Adaptive(minItemWidth)
}

fun gridColumnsForWidth(widthClass: ScreenWidthClass): Int = when (widthClass) {
    ScreenWidthClass.Compact -> 3
    ScreenWidthClass.Medium -> 4
    ScreenWidthClass.Expanded -> 6
}

fun gridColumnsForWidthPoster(widthClass: ScreenWidthClass): Int = when (widthClass) {
    ScreenWidthClass.Compact -> 2
    ScreenWidthClass.Medium -> 3
    ScreenWidthClass.Expanded -> 4
}

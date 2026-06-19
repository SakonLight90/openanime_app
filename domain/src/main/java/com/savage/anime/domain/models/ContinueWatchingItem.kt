package com.savage.anime.domain.models

data class ContinueWatchingItem(
    val animeId: Int,
    val episodeId: Int,
    val positionMs: Long,
    val durationMs: Long = 0L,
    val lastWatchedAt: Long,
    val animeTitle: String,
    val animeImage: String,
    val episodeNumber: Double
)

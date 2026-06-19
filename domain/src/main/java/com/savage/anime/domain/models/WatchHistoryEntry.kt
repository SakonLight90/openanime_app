package com.savage.anime.domain.models

data class WatchHistoryEntry(
    val animeId: Int,
    val episodeId: Int,
    val watchedAt: Long,
    val animeTitle: String,
    val animeImage: String,
    val episodeNumber: Double
)

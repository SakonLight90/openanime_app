package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo

data class ContinueWatchingWithDetails(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "episode_id") val episodeId: Int,
    @ColumnInfo(name = "position_ms") val positionMs: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long = 0L,
    @ColumnInfo(name = "last_watched_at") val lastWatchedAt: Long,
    @ColumnInfo(name = "animeTitle") val animeTitle: String,
    @ColumnInfo(name = "animeImage") val animeImage: String,
    @ColumnInfo(name = "episodeNumber") val episodeNumber: Double,
    @ColumnInfo(name = "episodeTitle") val episodeTitle: String
)

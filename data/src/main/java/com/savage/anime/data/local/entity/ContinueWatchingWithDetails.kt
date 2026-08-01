package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo

data class ContinueWatchingWithDetails(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "episode_id") val episodeId: Int,
    @ColumnInfo(name = "position_ms") val positionMs: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long = 0L,
    @ColumnInfo(name = "last_watched_at") val lastWatchedAt: Long,
    @ColumnInfo(name = "anime_title") val animeTitle: String,
    @ColumnInfo(name = "anime_image") val animeImage: String,
    @ColumnInfo(name = "episode_number") val episodeNumber: Double
)

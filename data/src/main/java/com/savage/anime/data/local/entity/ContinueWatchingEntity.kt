package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "continue_watching",
    primaryKeys = ["anime_id", "episode_id"]
)
data class ContinueWatchingEntity(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "episode_id", defaultValue = "0") val episodeId: Int,
    @ColumnInfo(name = "position_ms", defaultValue = "0") val positionMs: Long,
    @ColumnInfo(name = "duration_ms", defaultValue = "0") val durationMs: Long = 0L,
    @ColumnInfo(name = "last_watched_at", defaultValue = "0") val lastWatchedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "anime_title", defaultValue = "''") val animeTitle: String = "",
    @ColumnInfo(name = "anime_image", defaultValue = "''") val animeImage: String = "",
    @ColumnInfo(name = "episode_number", defaultValue = "0") val episodeNumber: Double = 0.0
)

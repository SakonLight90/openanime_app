package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "continue_watching",
    primaryKeys = ["anime_id", "episode_id"]
)
data class ContinueWatchingEntity(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "episode_id") val episodeId: Int,
    @ColumnInfo(name = "position_ms") val positionMs: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long = 0L,
    @ColumnInfo(name = "last_watched_at") val lastWatchedAt: Long = System.currentTimeMillis()
)

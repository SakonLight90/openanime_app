package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "watch_history",
    primaryKeys = ["anime_id", "episode_id"]
)
data class WatchHistoryEntity(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "episode_id") val episodeId: Int,
    @ColumnInfo(name = "anime_title") val animeTitle: String,
    @ColumnInfo(name = "anime_image") val animeImage: String,
    @ColumnInfo(name = "episode_number", defaultValue = "0") val episodeNumber: Double,
    @ColumnInfo(name = "watched_at", defaultValue = "0") val watchedAt: Long = System.currentTimeMillis()
)

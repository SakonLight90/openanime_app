package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "search_cache",
    primaryKeys = ["query", "anime_id"]
)
data class SearchCacheEntity(
    @ColumnInfo(name = "query") val query: String,
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "episode_count") val episodeCount: Int,
    @ColumnInfo(name = "rating") val rating: Float = 0f,
    @ColumnInfo(name = "release_date") val releaseDate: String? = null,
    @ColumnInfo(name = "status") val status: String = ""
)

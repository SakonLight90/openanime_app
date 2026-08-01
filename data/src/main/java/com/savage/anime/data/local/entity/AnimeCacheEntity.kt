package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "anime_cache", primaryKeys = ["id", "category"])
data class AnimeCacheEntity(
    val id: Int,
    val title: String,
    val synopsis: String?,
    val image: String,
    @ColumnInfo(name = "cover_image") val coverImage: String?,
    @ColumnInfo(name = "banner_image") val bannerImage: String?,
    val type: String,
    @ColumnInfo(name = "episode_count", defaultValue = "0") val episodeCount: Int,
    @ColumnInfo(name = "rating", defaultValue = "0") val rating: Float,
    @ColumnInfo(name = "release_date") val releaseDate: String?,
    val status: String?,
    @ColumnInfo(name = "is_dub", defaultValue = "0") val isDub: Boolean,
    val language: String?,
    val genres: String?,
    @ColumnInfo(name = "category", defaultValue = "''") val category: String = "",
    @ColumnInfo(name = "updated_at", defaultValue = "0")
    val updatedAt: Long = System.currentTimeMillis()
)

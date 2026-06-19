package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_cache")
data class AnimeCacheEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val synopsis: String?,
    val image: String,
    @ColumnInfo(name = "cover_image") val coverImage: String?,
    @ColumnInfo(name = "banner_image") val bannerImage: String?,
    val type: String,
    @ColumnInfo(name = "episode_count") val episodeCount: Int,
    val rating: Float,
    @ColumnInfo(name = "release_date") val releaseDate: String?,
    val status: String?,
    @ColumnInfo(name = "is_dub") val isDub: Boolean,
    val language: String?,
    val genres: String?,
    val category: String = "",
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

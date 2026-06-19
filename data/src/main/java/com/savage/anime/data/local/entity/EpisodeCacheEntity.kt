package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes_cache")
data class EpisodeCacheEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "anime_id") val animeId: Int,
    val number: Double,
    val title: String,
    val token: String
)

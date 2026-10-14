package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "language_preferences")
data class LanguagePreferenceEntity(
    @PrimaryKey
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "preferred_version_id") val preferredVersionId: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

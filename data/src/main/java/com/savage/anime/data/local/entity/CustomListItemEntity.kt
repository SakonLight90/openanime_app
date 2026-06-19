package com.savage.anime.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "custom_list_items",
    primaryKeys = ["list_id", "anime_id"]
)
data class CustomListItemEntity(
    @ColumnInfo(name = "list_id") val listId: Int,
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "position") val position: Int
)

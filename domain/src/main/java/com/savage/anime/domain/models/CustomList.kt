package com.savage.anime.domain.models

data class CustomList(
    val id: Int,
    val name: String,
    val createdAt: Long,
    val items: List<Anime> = emptyList()
)

data class CustomListItem(
    val listId: Int,
    val animeId: Int,
    val position: Int
)

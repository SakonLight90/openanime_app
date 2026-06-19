package com.savage.anime.data.local.entity

data class EpisodeWithAnime(
    val id: Int,
    val animeId: Int,
    val number: Double,
    val title: String,
    val token: String,
    val animeTitle: String,
    val animeImage: String
)

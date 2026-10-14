package com.savage.anime.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
    val slug: String
)
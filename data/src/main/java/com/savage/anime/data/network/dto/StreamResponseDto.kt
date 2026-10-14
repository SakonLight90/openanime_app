package com.savage.anime.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StreamResponseDto(
    val success: Boolean,
    val url: String? = null,
    val error: String? = null
)

package com.savage.anime.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class StreamResponse(
    val success: Boolean,
    val url: String? = null,
    val error: String? = null
)

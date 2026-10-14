package com.savage.anime.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppVersionResponse(
    val currentVersion: String,
    val minCompatibleVersion: String,
    val updateUrl: String
)

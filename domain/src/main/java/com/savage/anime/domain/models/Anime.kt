package com.savage.anime.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Anime(
    val id: Int,
    val title: String,
    val image: String,
    val type: String,
    val episodeCount: Int,
    val rating: Float = 0f,
    val releaseDate: String? = null,
    val status: String = ""
)

@Serializable
data class AnimeDetail(
    val id: Int,
    val title: String,
    val synopsis: String,
    val genres: List<String>,
    val rating: Float,
    val episodeCount: Int,
    val type: String,
    val status: String,
    val isDub: Boolean,
    val language: String,
    val coverImage: String,
    val bannerImage: String,
    val relatedVersions: List<RelatedVersion>,
    val episodes: List<Episode>,
    val releaseDate: String? = null
)

@Serializable
data class RelatedVersion(
    val id: Int,
    val title: String,
    val language: String,
    val isDub: Boolean
)

@Serializable
data class Episode(
    val id: Int,
    val number: Double,
    val token: String,
    val language: String = "",
    val anime: AnimeRef? = null
)

@Serializable
data class AnimeRef(
    val id: Int,
    val title: String,
    val image: String
)

@Serializable
data class Season(
    val seasonNumber: Int,
    val episodes: List<Episode>,
    val episodeRange: String
)

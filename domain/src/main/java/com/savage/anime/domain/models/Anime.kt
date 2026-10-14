package com.savage.anime.domain.models

@kotlinx.serialization.Serializable
data class Anime(
    val id: Int,
    val title: String,
    val image: String,
    val type: String,
    val episodeCount: Int,
    val rating: Float = 0f,
    val releaseDate: String? = null,
    val status: String = "",
    val language: String = "jp"
) {
    val releaseYear: Int?
        get() = releaseDate?.take(4)?.toIntOrNull()
}

@kotlinx.serialization.Serializable
data class AnimeDetail(
    val id: Int,
    val title: String,
    val synopsis: String = "",
    val genres: List<String> = emptyList(),
    val rating: Float = 0f,
    val episodeCount: Int = 0,
    val type: String = "anime",
    val status: String = "",
    val isDub: Boolean = false,
    val language: String = "",
    val coverImage: String = "",
    val bannerImage: String = "",
    val releaseDate: String? = null,
    val relatedVersions: List<RelatedVersion> = emptyList(),
    val episodes: List<Episode> = emptyList()
)

@kotlinx.serialization.Serializable
data class RelatedVersion(
    val id: Int,
    val title: String,
    val language: String = "",
    val isDub: Boolean = false
)

@kotlinx.serialization.Serializable
data class Episode(
    val id: Int,
    val number: Double,
    val token: String = "",
    val language: String = "",
    val anime: AnimeRef? = null
)

@kotlinx.serialization.Serializable
data class AnimeRef(
    val id: Int,
    val title: String,
    val image: String = ""
)

@kotlinx.serialization.Serializable
data class Season(
    val seasonNumber: Int,
    val episodes: List<Episode>,
    val episodeRange: String
)

@kotlinx.serialization.Serializable
data class PagedAnime(
    val items: List<Anime> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 0,
    val total: Int = 0,
    val totalPages: Int = 0
) {
    val hasMore: Boolean
        get() = page < totalPages
}

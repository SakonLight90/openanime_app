package com.savage.anime.data.mapper

import com.savage.anime.data.local.entity.AnimeCacheEntity
import com.savage.anime.data.local.entity.EpisodeCacheEntity
import com.savage.anime.data.local.entity.SearchCacheEntity
import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.AnimeDetail
import com.savage.anime.domain.models.AnimeRef
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.util.cleanAnimeTitle

fun AnimeCacheEntity.toDomain(): Anime {
    return Anime(
        id = id,
        title = cleanAnimeTitle(title),
        image = image,
        type = type,
        episodeCount = episodeCount,
        rating = rating,
        releaseDate = releaseDate,
        status = status ?: ""
    )
}

fun AnimeCacheEntity.toDomainDetail(): AnimeDetail {
    return AnimeDetail(
        id = id,
        title = title,
        synopsis = synopsis ?: "",
        genres = genres?.split(",")?.mapNotNull { it.trim().ifBlank { null } } ?: emptyList(),
        rating = rating,
        episodeCount = episodeCount,
        type = type,
        status = status ?: "",
        isDub = isDub,
        language = language ?: "",
        coverImage = coverImage ?: image,
        bannerImage = bannerImage ?: "",
        releaseDate = releaseDate,
        relatedVersions = emptyList(),
        episodes = emptyList()
    )
}

fun Anime.toCacheEntity(category: String = ""): AnimeCacheEntity {
    return AnimeCacheEntity(
        id = id,
        title = title,
        synopsis = null,
        image = image,
        coverImage = null,
        bannerImage = null,
        type = type,
        episodeCount = episodeCount,
        rating = rating,
        releaseDate = releaseDate,
        status = status.ifEmpty { null },
        isDub = false,
        language = null,
        genres = null,
        category = category
    )
}

fun AnimeDetail.toCacheEntity(category: String = ""): AnimeCacheEntity {
    return AnimeCacheEntity(
        id = id,
        title = title,
        synopsis = synopsis,
        image = coverImage,
        coverImage = coverImage,
        bannerImage = bannerImage,
        type = type,
        episodeCount = episodeCount,
        rating = rating,
        releaseDate = releaseDate,
        status = status,
        isDub = isDub,
        language = language,
        genres = genres.joinToString(","),
        category = category
    )
}

fun SearchCacheEntity.toDomain(): Anime {
    return Anime(
        id = animeId,
        title = title,
        image = image,
        type = type,
        episodeCount = episodeCount,
        rating = rating,
        releaseDate = releaseDate,
        status = status
    )
}

fun Anime.toSearchCacheEntity(query: String): SearchCacheEntity {
    return SearchCacheEntity(
        query = query,
        animeId = id,
        title = title,
        image = image,
        type = type,
        episodeCount = episodeCount,
        rating = rating,
        releaseDate = releaseDate,
        status = status
    )
}

fun Episode.toCacheEntity(animeId: Int): EpisodeCacheEntity {
    return EpisodeCacheEntity(
        id = id,
        animeId = animeId,
        number = number,
        title = "",
        token = token
    )
}

fun com.savage.anime.data.local.entity.EpisodeWithAnime.toDomainEpisode(): Episode {
    return Episode(
        id = id,
        number = number,
        token = token,
        anime = AnimeRef(animeId, animeTitle, animeImage)
    )
}

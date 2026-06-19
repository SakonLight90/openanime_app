package com.savage.anime.data.network.dto

import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.Episode
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val hero: List<Anime>,
    val continueWatching: List<Anime>,
    val latestEpisodes: List<Episode>,
    val popular: List<Anime>,
    val ongoing: List<Anime>,
    val upcoming: List<Anime>,
    val newest: List<Anime>
)

package com.savage.anime.ui.screens.home

import com.savage.anime.domain.models.Anime
import com.savage.anime.domain.models.ContinueWatchingItem
import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.models.Genre

data class GenreSection(
    val genre: Genre,
    val items: List<Anime>
)

data class HomeUiState(
    val hero: List<Anime> = emptyList(),
    val popular: List<Anime> = emptyList(),
    val ongoing: List<Anime> = emptyList(),
    val upcoming: List<Anime> = emptyList(),
    val newest: List<Anime> = emptyList(),
    val updated: List<Anime> = emptyList(),
    val latestEpisodes: List<Episode> = emptyList(),
    val watchlist: List<Anime> = emptyList(),
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val genreSections: List<GenreSection> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

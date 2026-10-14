package com.savage.anime.ui.screens.genres

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.savage.anime.R
import com.savage.anime.domain.models.Anime
import com.savage.anime.navigation.detailRoute
import com.savage.anime.ui.components.AnimeGridScreen

@Composable
fun GenreAnimeScreen(
    navController: NavController,
    genreId: Int,
    genreName: String,
    modifier: Modifier = Modifier,
    viewModel: GenreAnimeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(genreId) {
        viewModel.loadGenreAnime(genreId)
    }

    AnimeGridScreen(
        title = genreName,
        items = state.items,
        isLoading = state.isLoading,
        error = state.error,
        totalCount = state.total,
        displayLimit = state.items.size,
        showMore = state.hasMore,
        onRetry = { viewModel.loadGenreAnime(genreId) },
        onShowMore = { viewModel.showMore() },
        onItemClick = { anime: Anime ->
            navController.navigate(detailRoute(anime.id))
        },
        emptyMessageRes = R.string.genre_anime_empty,
        modifier = modifier
    )
}

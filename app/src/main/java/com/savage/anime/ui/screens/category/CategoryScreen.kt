package com.savage.anime.ui.screens.category

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
fun CategoryScreen(
    navController: NavController,
    category: String,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(category) {
        viewModel.loadCategory(category)
    }

    AnimeGridScreen(
        title = category,
        items = state.items,
        isLoading = state.isLoading,
        error = state.error,
        totalCount = state.allItems.size,
        displayLimit = state.displayLimit,
        showMore = state.allItems.size > state.displayLimit,
        onRetry = { viewModel.loadCategory(category) },
        onShowMore = { viewModel.showMore() },
        onItemClick = { anime: Anime ->
            navController.navigate(detailRoute(anime.id))
        },
        emptyMessageRes = R.string.search_no_results,
        modifier = modifier
    )
}

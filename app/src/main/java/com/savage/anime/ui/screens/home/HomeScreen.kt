package com.savage.anime.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.savage.anime.R
import com.savage.anime.navigation.Screen
import com.savage.anime.navigation.categoryRoute
import com.savage.anime.navigation.detailRoute
import com.savage.anime.navigation.genreAnimeRoute
import com.savage.anime.navigation.playerRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val fetchError by viewModel.fetchError.collectAsState()
    val genreSections by viewModel.genreSections.collectAsState()
    val listState = rememberLazyListState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading && state.hero.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(key = "spacer_top") { Spacer(Modifier.height(4.dp)) }

state.error?.let { error ->
                        item(key = "error") {
                            Text(
                                text = error,
                                color = Color(0xFFB3B3B3),
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }

                    fetchError?.let { error ->
                        item(key = "fetch_error") {
                            Text(
                                text = error,
                                color = Color(0xFFB3B3B3),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (state.hero.isNotEmpty()) {
                        item(key = "hero") {
                            HeroBanner(
                                animeList = state.hero,
                                onItemClick = { animeId ->
                                    navController.navigate(detailRoute(animeId))
                                },
                                onWatchClick = { animeId ->
                                    val resumeItem = state.continueWatching.firstOrNull { it.animeId == animeId }
                                    if (resumeItem != null) {
                                        navController.navigate(playerRoute(resumeItem.animeId, resumeItem.episodeId))
                                    } else {
                                        navController.navigate(detailRoute(animeId))
                                    }
                                }
                            )
                        }
                    }

                    if (state.continueWatching.isNotEmpty()) {
                        item(key = "continue_watching") {
                            Spacer(Modifier.height(16.dp))
                            ContinueWatchingRow(
                                items = state.continueWatching,
                                onClick = { item ->
                                    navController.navigate(playerRoute(item.animeId, item.episodeId))
                                },
                                onRemove = { item ->
                                    viewModel.removeFromContinueWatching(item)
                                },
                                onViewAllClick = {
                                    navController.navigate(Screen.ContinueWatching.route)
                                }
                            )
                        }
                    }

                    if (state.latestEpisodes.isNotEmpty()) {
                        item(key = "latest_episodes") {
                            Spacer(Modifier.height(24.dp))
                            LatestEpisodesRow(
                                items = state.latestEpisodes.take(50),
                                onItemClick = { animeId, episodeId ->
                                    navController.navigate(playerRoute(animeId, episodeId))
                                },
                                onViewAllClick = {
                                    navController.navigate(Screen.LatestEpisodesAll.route)
                                }
                            )
                        }
                    }

                    if (state.watchlist.isNotEmpty()) {
                        item(key = "watchlist") {
                            Spacer(Modifier.height(24.dp))
                            WatchlistRow(
                                items = state.watchlist,
                                onItemClick = { animeId ->
                                    navController.navigate(detailRoute(animeId))
                                }
                            )
                        }
                    }

                    if (state.popular.isNotEmpty()) {
                        item(key = "row_popular") {
                            Spacer(Modifier.height(24.dp))
                            AnimeRow(
                                title = stringResource(R.string.section_popular),
                                items = state.popular,
                                onItemClick = { animeId ->
                                    navController.navigate(detailRoute(animeId))
                                },
                                onViewAllClick = {
                                    navController.navigate(categoryRoute("Popolari"))
                                }
                            )
                        }
                    }

                    if (state.updated.isNotEmpty()) {
                        item(key = "row_updated") {
                            Spacer(Modifier.height(24.dp))
                            AnimeRow(
                                title = stringResource(R.string.section_updated),
                                items = state.updated,
                                onItemClick = { animeId ->
                                    navController.navigate(detailRoute(animeId))
                                },
                                onViewAllClick = {
                                    navController.navigate(categoryRoute("Aggiornati"))
                                }
                            )
                        }
                    }

                    if (state.ongoing.isNotEmpty()) {
                        item(key = "row_ongoing") {
                            Spacer(Modifier.height(24.dp))
                            AnimeRow(
                                title = stringResource(R.string.section_ongoing),
                                items = state.ongoing,
                                onItemClick = { animeId ->
                                    navController.navigate(detailRoute(animeId))
                                },
                                onViewAllClick = {
                                    navController.navigate(categoryRoute("In Corso"))
                                }
                            )
                        }
                    }

                    if (state.newest.isNotEmpty()) {
                        item(key = "row_newest") {
                            Spacer(Modifier.height(24.dp))
                            AnimeRow(
                                title = stringResource(R.string.section_newest),
                                items = state.newest,
                                onItemClick = { animeId ->
                                    navController.navigate(detailRoute(animeId))
                                },
                                onViewAllClick = {
                                    navController.navigate(categoryRoute("Nuovi"))
                                }
                            )
                        }
                    }

                    genreSections.forEach { section ->
                        if (section.items.isNotEmpty()) {
                            item(key = "genre_${section.genre.id}") {
                                Spacer(Modifier.height(24.dp))
                                AnimeRow(
                                    title = section.genre.name,
                                    items = section.items,
                                    onItemClick = { animeId ->
                                        navController.navigate(detailRoute(animeId))
                                    },
                                    onViewAllClick = {
                                        navController.navigate(
                                            genreAnimeRoute(section.genre.id, section.genre.name)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    item(key = "spacer_bottom") { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

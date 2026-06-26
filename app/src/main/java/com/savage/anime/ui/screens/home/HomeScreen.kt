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
                    item { Spacer(Modifier.height(8.dp)) }

                    state.error?.let { error ->
                        item {
                            Text(
                                text = error,
                                color = Color(0xFFB3B3B3),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    fetchError?.let { error ->
                        item {
                            Text(
                                text = error,
                                color = Color(0xFFB3B3B3),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (state.hero.isNotEmpty()) {
                        item {
                            HeroBanner(
                                animeList = state.hero,
                                onItemClick = { animeId ->
                                    navController.navigate(
                                        Screen.Detail.route.replace("{animeId}", animeId.toString())
                                    )
                                }
                            )
                        }
                    }

                    if (state.continueWatching.isNotEmpty()) {
                        item {
                            ContinueWatchingRow(
                                items = state.continueWatching,
                                onClick = { item ->
                                    navController.navigate("player/${item.animeId}/${item.episodeId}")
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
                        item {
                            LatestEpisodesRow(
                                items = state.latestEpisodes.take(50),
                                onItemClick = { animeId, episodeId ->
                                    navController.navigate("player/$animeId/$episodeId")
                                },
                                onViewAllClick = {
                                    navController.navigate(Screen.LatestEpisodesAll.route)
                                }
                            )
                        }
                    }

                    if (state.updated.isNotEmpty()) {
                        item {
                            AnimeRow(
                                title = stringResource(R.string.section_updated),
                                items = state.updated,
                                onItemClick = { animeId ->
                                    navController.navigate(
                                        Screen.Detail.route.replace("{animeId}", animeId.toString())
                                    )
                                },
                                onViewAllClick = {
                                    navController.navigate(Screen.Category.route.replace("{category}", "Aggiornati"))
                                }
                            )
                        }
                    }

                    if (state.watchlist.isNotEmpty()) {
                        item {
                            WatchlistRow(
                                items = state.watchlist,
                                onItemClick = { animeId ->
                                    navController.navigate(
                                        Screen.Detail.route.replace("{animeId}", animeId.toString())
                                    )
                                }
                            )
                        }
                    }

                    if (state.popular.isNotEmpty()) {
                        item {
                            AnimeRow(
                                title = stringResource(R.string.section_popular),
                                items = state.popular,
                                onItemClick = { animeId ->
                                    navController.navigate(
                                        Screen.Detail.route.replace("{animeId}", animeId.toString())
                                    )
                                },
                                onViewAllClick = {
                                    navController.navigate(Screen.Category.route.replace("{category}", "Popolari"))
                                }
                            )
                        }
                    }

                    if (state.ongoing.isNotEmpty()) {
                        item {
                            AnimeRow(
                                title = stringResource(R.string.section_ongoing),
                                items = state.ongoing,
                                onItemClick = { animeId ->
                                    navController.navigate(
                                        Screen.Detail.route.replace("{animeId}", animeId.toString())
                                    )
                                },
                                onViewAllClick = {
                                    navController.navigate(Screen.Category.route.replace("{category}", "In Corso"))
                                }
                            )
                        }
                    }

                    if (state.upcoming.isNotEmpty()) {
                        item {
                            AnimeRow(
                                title = stringResource(R.string.section_upcoming),
                                items = state.upcoming,
                                onItemClick = { animeId ->
                                    navController.navigate(
                                        Screen.Detail.route.replace("{animeId}", animeId.toString())
                                    )
                                },
                                onViewAllClick = {
                                    navController.navigate(Screen.Category.route.replace("{category}", "Imminenti"))
                                }
                            )
                        }
                    }

                    if (state.newest.isNotEmpty()) {
                        item {
                            AnimeRow(
                                title = stringResource(R.string.section_newest),
                                items = state.newest,
                                onItemClick = { animeId ->
                                    navController.navigate(
                                        Screen.Detail.route.replace("{animeId}", animeId.toString())
                                    )
                                },
                                onViewAllClick = {
                                    navController.navigate(Screen.Category.route.replace("{category}", "Nuovi"))
                                }
                            )
                        }
                    }

                    genreSections.forEach { section ->
                        if (section.items.isNotEmpty()) {
                            item {
                                AnimeRow(
                                    title = section.genre.name,
                                    items = section.items,
                                    onItemClick = { animeId ->
                                        navController.navigate(
                                            Screen.Detail.route.replace("{animeId}", animeId.toString())
                                        )
                                    },
                                    onViewAllClick = {
                                        navController.navigate(
                                            Screen.GenreAnime.route
                                                .replace("{genreId}", section.genre.id.toString())
                                                .replace("{genreName}", section.genre.name)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

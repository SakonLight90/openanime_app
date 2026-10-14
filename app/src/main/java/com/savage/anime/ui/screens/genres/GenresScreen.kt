package com.savage.anime.ui.screens.genres

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.savage.anime.R
import com.savage.anime.navigation.genreAnimeRoute

@Composable
fun GenresScreen(
    navController: NavController,
    viewModel: GenresViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadGenres()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        Column {
            Text(
                text = stringResource(R.string.genres_title),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.error ?: "", color = Color.White, fontSize = 16.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadGenres() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                state.genres.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.genres_empty), color = Color.Gray)
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.genres, key = { it.id }) { genre ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                                onClick = {
                                    navController.navigate(genreAnimeRoute(genre.id, genre.name))
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = genre.name,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = ">",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
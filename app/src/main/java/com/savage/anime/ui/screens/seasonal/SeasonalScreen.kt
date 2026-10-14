package com.savage.anime.ui.screens.seasonal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.savage.anime.navigation.detailRoute
import com.savage.anime.ui.components.AnimeGridCard
import com.savage.anime.ui.utils.currentScreenWidthClass
import com.savage.anime.ui.utils.gridColumnsForWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonalScreen(
    navController: NavController,
    viewModel: SeasonalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.seasonal_title), color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141414))
            )
        },
        containerColor = Color(0xFF141414)
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.error ?: "", color = Color.White, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadSeasonal() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            uiState.seasons.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.seasonal_empty), color = Color(0xFF666666), fontSize = 14.sp)
                }
            }

            else -> {
                val columns = gridColumnsForWidth(currentScreenWidthClass())
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.seasons.forEach { (seasonLabel, animeList) ->
                        item(key = "header_$seasonLabel", span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = seasonLabel,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(animeList, key = { "${seasonLabel}_${it.id}" }) { anime ->
                            AnimeGridCard(
                                anime = anime,
                                onClick = { navController.navigate(detailRoute(anime.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

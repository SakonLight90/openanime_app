package com.savage.anime.ui.screens.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
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
import com.savage.anime.domain.models.CustomList
import com.savage.anime.navigation.customListRoute
import com.savage.anime.navigation.detailRoute
import com.savage.anime.ui.components.AnimeGridCard
import com.savage.anime.ui.utils.currentScreenWidthClass
import com.savage.anime.ui.utils.gridColumnsForWidth

@Composable
fun WatchlistScreen(
    navController: NavController,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadWatchlist()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        if (uiState.watchlist.isEmpty() && uiState.customLists.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.watchlist_empty),
                    color = Color(0xFFB3B3B3),
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.watchlist_empty_hint),
                    color = Color(0xFF666666),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            val columns = gridColumnsForWidth(currentScreenWidthClass())
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.watchlist_title),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (uiState.watchlist.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionLabel(stringResource(R.string.watchlist_favorites))
                    }

                    items(uiState.watchlist, key = { it.id }) { anime ->
                        AnimeGridCard(
                            anime = anime,
                            onClick = { navController.navigate(detailRoute(anime.id)) }
                        )
                    }
                }

                if (uiState.customLists.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionLabel(
                            text = stringResource(R.string.watchlist_my_lists),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    uiState.customLists.forEach { list ->
                        item(key = "list_${list.id}", span = { GridItemSpan(maxLineSpan) }) {
                            CustomListRow(
                                list = list,
                                onClick = { navController.navigate(customListRoute(list.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = Color(0xFFB3B3B3),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun CustomListRow(
    list: CustomList,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(list.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(">", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
        }
    }
}

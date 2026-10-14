package com.savage.anime.ui.screens.continuewatching

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.savage.anime.R
import com.savage.anime.domain.models.ContinueWatchingItem
import com.savage.anime.navigation.Screen
import com.savage.anime.navigation.playerRoute
import com.savage.anime.ui.utils.currentScreenWidthClass
import com.savage.anime.ui.utils.gridColumnsForWidthPoster
import com.savage.anime.utils.formatEpisodeNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinueWatchingScreen(
    navController: NavController,
    viewModel: ContinueWatchingViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.continue_watching_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141414))
            )
        },
        containerColor = Color(0xFF141414)
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.continue_watching_empty),
                    color = Color(0xFFB3B3B3),
                    fontSize = 16.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumnsForWidthPoster(currentScreenWidthClass())),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(items, key = { it.episodeId }) { item ->
                    var showMenu by rememberSaveable(item.episodeId) { mutableStateOf(false) }

                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        playerRoute(item.animeId, item.episodeId)
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Column {
                                AsyncImage(
                                    model = item.animeImage,
                                    contentDescription = item.animeTitle,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = item.animeTitle,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.episode_prefix, formatEpisodeNumber(item.episodeNumber)),
                                        color = Color(0xFF666666),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.more_options),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(0.dp, 0.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remove)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.removeFromContinueWatching(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

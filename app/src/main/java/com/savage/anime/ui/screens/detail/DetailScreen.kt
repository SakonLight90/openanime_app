package com.savage.anime.ui.screens.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.savage.anime.R
import com.savage.anime.ui.screens.detail.components.LanguageSelector
import com.savage.anime.ui.screens.detail.components.PlayButton
import com.savage.anime.ui.screens.detail.components.Season
import com.savage.anime.ui.screens.detail.components.SeasonSection
import com.savage.anime.utils.formatEpisodeNumber
import com.savage.anime.utils.groupEpisodesIntoSeasons

@Composable
fun DetailScreen(
    navController: NavController,
    animeId: Int,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(animeId) {
        viewModel.loadDetail(animeId)
        viewModel.loadWatchlistStatus(animeId)
        viewModel.loadCustomLists()
    }

    val anime = state.anime

    val domainSeasons = remember(anime) { groupEpisodesIntoSeasons(anime?.episodes ?: emptyList()) }
    var selectedSeasonIndex by remember(anime?.id) { mutableStateOf(0) }
    if (selectedSeasonIndex >= domainSeasons.size && domainSeasons.isNotEmpty()) {
        selectedSeasonIndex = 0
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        if (anime == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(state.error ?: stringResource(R.string.detail_not_found), color = Color.Gray)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp)
                        ) {
                            AsyncImage(
                                model = anime.coverImage,
                                contentDescription = anime.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0xFF141414))
                                        )
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .align(Alignment.TopCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0x99000000), Color.Transparent)
                                        )
                                    )
                            )

                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 8.dp, top = 8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x66000000))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 8.dp, top = 8.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.addToCustomList() },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x66000000))
                                ) {
                                    Icon(
                                        imageVector = if (state.isInWatchlist) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = if (state.isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                                        tint = if (state.isInWatchlist) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                val shareTitle = stringResource(R.string.detail_share_title)
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "${anime.title}\nopenanime://anime/$animeId")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, shareTitle))
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x66000000))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = shareTitle,
                                        tint = Color.White
                                    )
                                }
                            }

                            Text(
                                text = anime.title,
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFCC00),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = anime.rating.toString(),
                                    color = Color(0xFFB3B3B3),
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = anime.type,
                                color = Color(0xFFB3B3B3),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${anime.episodeCount} ep.",
                                color = Color(0xFFB3B3B3),
                                fontSize = 14.sp
                            )
                            Text(
                                text = anime.status,
                                color = Color(0xFFB3B3B3),
                                fontSize = 14.sp
                            )
                        }
                    }

                    if (anime.genres.isNotEmpty()) {
                        item {
                            Text(
                                text = anime.genres.joinToString(" \u00B7 "),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(onClick = { viewModel.addToCustomList() }) {
                                Text("+ Aggiungi a lista", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            }
                        }
                    }

                    if (anime.episodes.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PlayButton(
                                    onClick = {
                                        val firstEpisode = anime.episodes.first()
                                        navController.navigate("player/${state.currentDisplayId}/${firstEpisode.id}")
                                    }
                                )
                                val lastWatched = state.lastWatchedEpisode
                                if (lastWatched != null) {
                                    Button(
                                        onClick = {
                                            navController.navigate("player/${state.currentDisplayId}/${lastWatched.id}")
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF333333),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Continua episodio ${formatEpisodeNumber(lastWatched.number)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Trama",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = anime.synopsis,
                                color = Color(0xFFB3B3B3),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)) {
                            Text(
                                text = stringResource(R.string.detail_version),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            LanguageSelector(
                                selectedVersionId = state.selectedVersionId,
                                currentAnimeId = anime.id,
                                currentTitle = anime.title,
                                currentIsDub = anime.isDub,
                                versions = anime.relatedVersions,
                                onLanguageSelected = { viewModel.selectLanguage(it) }
                            )
                        }
                    }

                    if (domainSeasons.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.detail_episodes),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        if (domainSeasons.size > 1) {
                            item {
                                var expanded by remember { mutableStateOf(false) }
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { expanded = true }
                                                .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Seleziona Stagione",
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                            Icon(
                                                Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Seleziona stagione",
                                                tint = Color(0xFF999999)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            domainSeasons.forEachIndexed { index, ds ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            stringResource(R.string.detail_season_format, ds.seasonNumber, ds.episodeRange),
                                                            color = if (index == selectedSeasonIndex) MaterialTheme.colorScheme.primary else Color.White
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedSeasonIndex = index
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            val ds = domainSeasons[selectedSeasonIndex]
                            SeasonSection(
                                season = Season(
                                    name = stringResource(R.string.detail_season_format, ds.seasonNumber, ds.episodeRange),
                                    episodes = ds.episodes
                                ),
                                watchedEpisodeIds = state.watchedEpisodeIds,
                                episodeProgress = state.episodeProgress,
                                onEpisodeClick = { episode ->
                                    navController.navigate("player/${state.currentDisplayId}/${episode.id}")
                                },
                                onLongClick = { episode ->
                                    viewModel.toggleEpisodeWatched(episode.id)
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }

                if (state.showListPicker) {
                    AlertDialog(
                        onDismissRequest = { viewModel.hideCustomListPicker() },
                        containerColor = Color(0xFF1A1A1A),
                        title = { Text("Aggiungi a lista", color = Color.White) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                state.customLists.forEach { list ->
                                    TextButton(
                                        onClick = { viewModel.addToCustomList(list.id) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(list.name, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.hideCustomListPicker() }) {
                                Text("Annulla", color = Color(0xFFB3B3B3))
                            }
                        }
                    )
                }
            }
        }
    }
}

package com.savage.anime.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.savage.anime.R
import com.savage.anime.domain.models.Anime
import com.savage.anime.ui.utils.currentScreenWidthClass
import com.savage.anime.ui.utils.gridColumnsForWidth

@Composable
fun AnimeGridCard(
    anime: Anime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        onClick = onClick
    ) {
        Column {
            AsyncImage(
                model = anime.image,
                contentDescription = anime.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = anime.title,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun AnimeGridLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun AnimeGridError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = Color.White, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
fun AnimeGridEmpty(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray)
    }
}

@Composable
fun AnimeGrid(
    items: List<Anime>,
    onItemClick: (Anime) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = gridColumnsForWidth(currentScreenWidthClass())
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(items, key = { it.id }) { anime ->
            AnimeGridCard(anime = anime, onClick = { onItemClick(anime) })
        }
    }
}

@Composable
fun AnimeGridScreen(
    title: String,
    items: List<Anime>,
    isLoading: Boolean,
    error: String?,
    totalCount: Int = items.size,
    displayLimit: Int = items.size,
    showMore: Boolean = false,
    isLoadingMore: Boolean = false,
    onRetry: () -> Unit,
    onShowMore: () -> Unit,
    onItemClick: (Anime) -> Unit,
    emptyMessageRes: Int = R.string.search_no_results,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF141414)
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )

            when {
                isLoading -> AnimeGridLoading()

                error != null -> AnimeGridError(message = error, onRetry = onRetry)

                items.isEmpty() -> AnimeGridEmpty(message = stringResource(emptyMessageRes))

                else -> {
                    val columns = gridColumnsForWidth(currentScreenWidthClass())
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items, key = { it.id }) { anime ->
                            AnimeGridCard(anime = anime, onClick = { onItemClick(anime) })
                        }
                        if (showMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Button(
                                    onClick = onShowMore,
                                    enabled = !isLoadingMore,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (isLoadingMore) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            stringResource(R.string.show_more, (totalCount - displayLimit).coerceAtLeast(0)),
                                            color = Color.White
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
}

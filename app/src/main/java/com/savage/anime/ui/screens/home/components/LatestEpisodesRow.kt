package com.savage.anime.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.savage.anime.R
import com.savage.anime.domain.models.Episode
import com.savage.anime.utils.formatEpisodeNumber

@Composable
fun LatestEpisodesRow(
    items: List<Episode>,
    onItemClick: (Int, Int) -> Unit,
    onViewAllClick: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.section_latest_episodes),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (onViewAllClick != null) {
                Text(
                    text = stringResource(R.string.view_all),
                    color = Color(0x99FFFFFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(items.filter { it.anime != null }, key = { it.id }) { episode ->
                val animeRef = episode.anime ?: return@items
                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable {
                            onItemClick(animeRef.id, episode.id)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A1A1A))
                    ) {
                        AsyncImage(
                            model = animeRef.image,
                            contentDescription = animeRef.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color(0xCC141414))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.episode_prefix, formatEpisodeNumber(episode.number)),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = animeRef.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

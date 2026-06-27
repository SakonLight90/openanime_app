package com.savage.anime.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.savage.anime.domain.models.ContinueWatchingItem

@Composable
fun ContinueWatchingRow(
    items: List<ContinueWatchingItem>,
    onClick: (ContinueWatchingItem) -> Unit,
    onRemove: (ContinueWatchingItem) -> Unit = {},
    onViewAllClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Continua a guardare",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (onViewAllClick != null && items.isNotEmpty()) {
                Text(
                    text = "Vedi tutti",
                    color = Color(0x99FFFFFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                var showMenu by remember { mutableStateOf(false) }

                Box {
                    Column(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onClick(item) }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(240.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1A1A1A))
                        ) {
                            AsyncImage(
                                model = item.animeImage,
                                contentDescription = item.animeTitle,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(bottomEnd = 6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Ep. ${item.episodeNumber.toInt()}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            Color(0x88000000),
                                            RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = {
                                    if (item.durationMs > 0)
                                        item.positionMs.toFloat() / item.durationMs.toFloat()
                                    else 0f
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.BottomCenter),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color(0xFF333333)
                            )
                        }

                        Text(
                            text = item.animeTitle,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp, start = 2.dp)
                        )

                        Text(
                            text = "Ep. ${item.episodeNumber.toInt()} - Continua",
                            color = Color(0xAAFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        offset = DpOffset(0.dp, 0.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rimuovi") },
                            onClick = {
                                showMenu = false
                                onRemove(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

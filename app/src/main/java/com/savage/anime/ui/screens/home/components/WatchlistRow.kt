package com.savage.anime.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savage.anime.R
import com.savage.anime.domain.models.Anime
import com.savage.anime.ui.components.AnimePosterCard

@Composable
fun WatchlistRow(
    items: List<Anime>,
    onItemClick: (Int) -> Unit,
    onViewAllClick: (() -> Unit)? = null
) {
    if (items.isEmpty()) return

    Column {
        Text(
            text = stringResource(R.string.section_watchlist),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(items, key = { it.id }) { anime ->
                AnimePosterCard(
                    imageUrl = anime.image,
                    title = anime.title,
                    onClick = { onItemClick(anime.id) }
                )
            }
        }
    }
}

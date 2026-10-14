package com.savage.anime.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun AnimeRow(
    title: String,
    items: List<Anime>,
    onItemClick: (Int) -> Unit,
    onViewAllClick: (() -> Unit)? = null
) {
    if (items.isEmpty()) return

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
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

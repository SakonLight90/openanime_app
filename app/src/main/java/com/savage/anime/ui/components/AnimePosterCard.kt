package com.savage.anime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun AnimePosterCard(
    imageUrl: String?,
    title: String,
    onClick: () -> Unit,
    width: Dp = 140.dp,
    height: Dp = 210.dp,
    placeholderColor: Color = Color(0xFF1A1A1A),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(width)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(8.dp))
                .background(placeholderColor)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 2.dp)
        )
    }
}

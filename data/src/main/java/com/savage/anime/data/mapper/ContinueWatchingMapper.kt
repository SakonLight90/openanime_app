package com.savage.anime.data.mapper

import com.savage.anime.data.local.entity.ContinueWatchingWithDetails
import com.savage.anime.domain.models.ContinueWatchingItem

fun ContinueWatchingWithDetails.toDomain(): ContinueWatchingItem {
    return ContinueWatchingItem(
        animeId = animeId,
        episodeId = episodeId,
        positionMs = positionMs,
        durationMs = durationMs,
        lastWatchedAt = lastWatchedAt,
        animeTitle = animeTitle,
        animeImage = animeImage,
        episodeNumber = episodeNumber
    )
}

package com.savage.anime.data.mapper

import com.savage.anime.data.local.entity.WatchHistoryEntity
import com.savage.anime.domain.models.WatchHistoryEntry

fun WatchHistoryEntity.toDomain(): WatchHistoryEntry {
    return WatchHistoryEntry(
        animeId = animeId,
        episodeId = episodeId,
        watchedAt = watchedAt,
        animeTitle = animeTitle,
        animeImage = animeImage,
        episodeNumber = episodeNumber
    )
}

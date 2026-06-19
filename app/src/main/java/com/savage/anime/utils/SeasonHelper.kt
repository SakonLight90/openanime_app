package com.savage.anime.utils

import com.savage.anime.domain.models.Episode
import com.savage.anime.domain.models.Season

fun groupEpisodesIntoSeasons(episodes: List<Episode>): List<Season> {

    if (episodes.isEmpty()) return emptyList()

    val sorted = episodes.sortedBy { it.number }

    if (sorted.size <= 30) {
        val range = "${formatEpisodeNumber(sorted.first().number)} - ${formatEpisodeNumber(sorted.last().number)}"
        return listOf(Season(1, sorted, range))
    }

    return sorted.chunked(30).mapIndexed { index, chunk ->
        val range = "${formatEpisodeNumber(chunk.first().number)} - ${formatEpisodeNumber(chunk.last().number)}"
        Season(
            seasonNumber = index + 1,
            episodes = chunk,
            episodeRange = range
        )
    }
}

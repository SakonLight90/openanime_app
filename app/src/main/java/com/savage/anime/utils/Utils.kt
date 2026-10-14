package com.savage.anime.utils

fun formatEpisodeNumber(number: Double): String {
    return if (number == number.toInt().toDouble()) {
        number.toInt().toString()
    } else {
        number.toString()
    }
}

fun episodeDisplayTitle(number: Double): String {
    return "Episodio ${formatEpisodeNumber(number)}"
}



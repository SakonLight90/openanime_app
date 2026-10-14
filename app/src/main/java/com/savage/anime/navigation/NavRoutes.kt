package com.savage.anime.navigation

import android.net.Uri

fun playerRoute(animeId: Int, episodeId: Int): String =
    Screen.Player.route
        .replace("{animeId}", animeId.toString())
        .replace("{episodeId}", episodeId.toString())

fun detailRoute(animeId: Int): String =
    Screen.Detail.route.replace("{animeId}", animeId.toString())

fun genreAnimeRoute(genreId: Int, genreName: String): String =
    Screen.GenreAnime.route
        .replace("{genreId}", genreId.toString())
        .replace("{genreName}", Uri.encode(genreName))

fun azListRoute(letter: String): String =
    Screen.AzList.route.replace("{letter}", letter)

fun categoryRoute(category: String): String =
    Screen.Category.route.replace("{category}", Uri.encode(category))

fun customListRoute(listId: Int): String =
    Screen.CustomListDetail.route.replace("{listId}", listId.toString())

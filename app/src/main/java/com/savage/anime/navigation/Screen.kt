package com.savage.anime.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Watchlist : Screen("watchlist")
    object Settings : Screen("settings")
    object AzList : Screen("az-list/{letter}")
    object Category : Screen("category/{category}")
    object Genres : Screen("genres")
    object GenreAnime : Screen("genre-anime/{genreId}/{genreName}")
    object Detail : Screen("detail/{animeId}")
    object Player : Screen("player/{animeId}/{episodeId}")
    object ContinueWatching : Screen("continue-watching")
    object Onboarding : Screen("onboarding")
    object History : Screen("history")
    object CustomLists : Screen("custom-lists")
    object CustomListDetail : Screen("custom-list/{listId}")
    object Seasonal : Screen("seasonal")
    object LatestEpisodesAll : Screen("latest-episodes-all")
}

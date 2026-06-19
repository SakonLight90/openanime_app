package com.savage.anime.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.savage.anime.R
import com.savage.anime.ui.screens.azlist.AzListScreen
import com.savage.anime.ui.screens.category.CategoryScreen
import com.savage.anime.ui.screens.customlists.CustomListDetailScreen
import com.savage.anime.ui.screens.customlists.CustomListsScreen
import com.savage.anime.ui.screens.detail.DetailScreen
import com.savage.anime.ui.screens.genres.GenreAnimeScreen
import com.savage.anime.ui.screens.genres.GenresScreen
import com.savage.anime.ui.screens.continuewatching.ContinueWatchingScreen
import com.savage.anime.ui.screens.history.HistoryScreen
import com.savage.anime.ui.screens.home.HomeScreen
import com.savage.anime.ui.screens.onboarding.OnboardingScreen
import com.savage.anime.ui.screens.player.PlayerScreen
import com.savage.anime.ui.screens.search.SearchScreen
import com.savage.anime.ui.screens.latestepisodes.AllLatestEpisodesScreen
import com.savage.anime.ui.screens.seasonal.SeasonalScreen
import com.savage.anime.ui.screens.settings.SettingsScreen
import com.savage.anime.ui.screens.watchlist.WatchlistScreen

@Composable
fun NavGraph(modifier: Modifier = Modifier) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val activity = LocalContext.current as? androidx.activity.ComponentActivity

    var lastDeepLinkUri by remember { mutableStateOf<Uri?>(null) }

    fun processDeepLink(uri: Uri?) {
        if (uri == null || uri == lastDeepLinkUri) return
        lastDeepLinkUri = uri
        val animeId = when {
            uri.scheme == "openanime" && uri.host == "anime" -> uri.pathSegments.firstOrNull()?.toIntOrNull()
            else -> null
        }
        if (animeId != null) {
            val detailRoute = Screen.Detail.route.replace("{animeId}", animeId.toString())
            if (currentRoute != detailRoute) {
                navController.navigate(detailRoute) { launchSingleTop = true }
            }
        }
    }

    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                processDeepLink(activity?.intent?.data)
            }
        }
        activity?.lifecycle?.addObserver(observer)
        processDeepLink(activity?.intent?.data)
        onDispose { activity?.lifecycle?.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("openanime_prefs", 0)
        if (!prefs.getBoolean("onboarding_done", false)) {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Home.route) { saveState = false }
            }
        }
    }

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Watchlist.route,
        Screen.Settings.route
    )
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onRouteSelected = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(
                route = Screen.Onboarding.route,
                enterTransition = { slideInHorizontally() + fadeIn() },
                exitTransition = { slideOutHorizontally() + fadeOut() }
            ) {
                OnboardingScreen(
                    onComplete = {
                        val prefs = context.getSharedPreferences("openanime_prefs", 0)
                        prefs.edit().putBoolean("onboarding_done", true).apply()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Home.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                HomeScreen(navController)
            }

            composable(
                route = Screen.Search.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                SearchScreen(
                    navController = navController,
                    onAzListClick = {
                        navController.navigate(Screen.AzList.route.replace("{letter}", "A"))
                    }
                )
            }

            composable(
                route = Screen.AzList.route,
                arguments = listOf(navArgument("letter") { type = NavType.StringType })
            ) { backStackEntry ->
                AzListScreen(navController = navController)
            }

            composable(
                route = Screen.Watchlist.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                WatchlistScreen(navController)
            }

            composable(
                route = Screen.Settings.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                SettingsScreen(navController)
            }

            composable(
                route = Screen.Category.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val category = requireNotNull(backStackEntry.arguments?.getString("category"))
                CategoryScreen(navController = navController, category = category)
            }

            composable(
                route = Screen.Genres.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                GenresScreen(navController = navController)
            }

            composable(
                route = Screen.GenreAnime.route,
                arguments = listOf(
                    navArgument("genreId") { type = NavType.IntType },
                    navArgument("genreName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val genreId = requireNotNull(backStackEntry.arguments?.getInt("genreId"))
                val genreName = requireNotNull(backStackEntry.arguments?.getString("genreName"))
                GenreAnimeScreen(
                    navController = navController,
                    genreId = genreId,
                    genreName = genreName
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("animeId") { type = NavType.IntType }),
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                exitTransition = { slideOutHorizontally { it } + fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { slideOutHorizontally { it } + fadeOut() }
            ) { backStackEntry ->

                val animeId = requireNotNull(backStackEntry.arguments?.getInt("animeId"))

                DetailScreen(
                    navController = navController,
                    animeId = animeId
                )
            }

            composable(
                route = Screen.ContinueWatching.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                ContinueWatchingScreen(navController = navController)
            }

            composable(
                route = Screen.Player.route,
                arguments = listOf(
                    navArgument("animeId") { type = NavType.IntType },
                    navArgument("episodeId") { type = NavType.IntType }
                ),
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                exitTransition = { slideOutHorizontally { it } + fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { slideOutHorizontally { it } + fadeOut() }
            ) { backStackEntry ->

                val animeId = requireNotNull(backStackEntry.arguments?.getInt("animeId"))
                val episodeId = requireNotNull(backStackEntry.arguments?.getInt("episodeId"))

                PlayerScreen(
                    navController = navController,
                    animeId = animeId,
                    episodeId = episodeId
                )
            }

            composable(
                route = Screen.History.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                HistoryScreen(navController = navController)
            }

            composable(
                route = Screen.CustomLists.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                CustomListsScreen(navController = navController)
            }

            composable(
                route = Screen.CustomListDetail.route,
                arguments = listOf(navArgument("listId") { type = NavType.IntType })
            ) { backStackEntry ->
                val listId = requireNotNull(backStackEntry.arguments?.getInt("listId"))
                CustomListDetailScreen(navController = navController, listId = listId)
            }

            composable(
                route = Screen.LatestEpisodesAll.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                AllLatestEpisodesScreen(navController = navController)
            }

            composable(
                route = Screen.Seasonal.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                SeasonalScreen(navController = navController)
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    currentRoute: String?,
    onRouteSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF141414)
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onRouteSelected(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.labelRes),
                        tint = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF666666)
                    )
                },
                label = {
                    if (selected) {
                        Text(stringResource(item.labelRes), color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}

private data class NavItem(val route: String, val labelRes: Int, val icon: ImageVector)

private val navItems = listOf(
    NavItem(Screen.Home.route, R.string.nav_home, Icons.Default.Home),
    NavItem(Screen.Search.route, R.string.nav_search, Icons.Default.Search),
    NavItem(Screen.Watchlist.route, R.string.nav_watchlist, Icons.Default.Favorite),
    NavItem(Screen.Settings.route, R.string.nav_settings, Icons.Default.Settings)
)

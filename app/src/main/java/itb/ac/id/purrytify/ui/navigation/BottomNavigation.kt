package itb.ac.id.purrytify.ui.navigation

//import itb.ac.id.purrytify.ui.library.LibraryFragment
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import itb.ac.id.purrytify.ui.home.HomeFragment
import itb.ac.id.purrytify.ui.library.LibraryScreen
import itb.ac.id.purrytify.ui.player.*
import itb.ac.id.purrytify.ui.profile.ProfileScreen

@Composable
fun MainScreen(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val songPlayerViewModel = hiltViewModel<SongPlayerViewModel>()
    val currentSong by songPlayerViewModel.currentSong.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            //        // Di desain figma ga ada header? kalo butuh nanti uncomment
//        topBar = {
//            currentRoute?.let { Header(currentRoute = it) }
//        },
            bottomBar = {
                Column {
                    if (currentSong != null && currentRoute != "track_view") {
                        MiniPlayer(
                            songPlayerViewModel,
                            onExpand = {
                                // Store current route
                                val currentScreen = currentRoute ?: NavigationItem.Home.route
                                Log.d("Navigation", "Storing current screen: $currentScreen")
                                songPlayerViewModel.setLastScreenRoute(currentScreen)
                                navController.navigate("track_view")
                            }
                        )
                    }
                    BottomNavigation(navController = navController, songPlayerViewModel = songPlayerViewModel)
                }
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                NavigationGraph(songPlayerViewModel, navController = navController)
            }
        }
    }
}

@Composable
fun NavigationGraph(songPlayerViewModel: SongPlayerViewModel, navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) {
            HomeFragment(songPlayerViewModel = songPlayerViewModel, onPlay = {
                songPlayerViewModel.setLastScreenRoute(NavigationItem.Home.route)
                navController.navigate("track_view")
            })
        }
        composable(NavigationItem.Library.route) {
            LibraryScreen(songPlayerViewModel, onPlay = {
                // Store library sebagai last screen
                songPlayerViewModel.setLastScreenRoute(NavigationItem.Library.route)
                navController.navigate("track_view")
            })
        }
        composable(NavigationItem.Profile.route) {
            ProfileScreen()
        }
        composable("track_view") {
            TrackViewFragment(
                viewModel = songPlayerViewModel,
                onBack = {
                    val lastRoute = songPlayerViewModel.getLastScreenRoute()
                    Log.d("Navigation", "Going back to last route: $lastRoute")

                    navController.popBackStack()
                    if (navController.currentBackStackEntry?.destination?.route != lastRoute) {
                        navController.navigate(lastRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController, songPlayerViewModel: SongPlayerViewModel) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Library,
        NavigationItem.Profile
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (currentRoute == item.route) {
                                item.selectedIconResId
                            } else {
                                item.iconResId
                            }
                        ),
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    Log.d("Navigation", "Clicked on: ${item.route}, current route: $currentRoute")
                    if (currentRoute == "track_view") {
                        songPlayerViewModel.setLastScreenRoute(item.route)
                        // Pop back utk remove track_view dari back stack
                        navController.popBackStack()
                        if (navController.currentBackStackEntry?.destination?.route != item.route) {
                            navController.navigate(item.route) {
                                // Pop up to the root to clear any nested navigation
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    } else {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent,
                )
            )
        }
    }
}
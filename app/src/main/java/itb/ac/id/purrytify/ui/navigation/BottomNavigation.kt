package itb.ac.id.purrytify.ui.navigation

//import itb.ac.id.purrytify.ui.library.LibraryFragment
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import itb.ac.id.purrytify.ui.home.HomeFragment
import itb.ac.id.purrytify.ui.library.LibraryScreen
import itb.ac.id.purrytify.ui.onlinesong.OnlineSongListScreen
import itb.ac.id.purrytify.ui.player.*
import itb.ac.id.purrytify.ui.profile.ProfileScreen
import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(navController: NavHostController, songPlayerViewModel: SongPlayerViewModel, deepLink: Uri?) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentSong by songPlayerViewModel.currentSong.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect (deepLink) {
        Log.d("MainScreen", "Deep link: $deepLink")
        val path = deepLink?.lastPathSegment
        if (path != null) {
            songPlayerViewModel.playOnlineSong(path)
            songPlayerViewModel.setLastScreenRoute(NavigationItem.Home.route)
            navController.navigate("track_view")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // landscape
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width((LocalConfiguration.current.screenWidthDp * 0.25f).dp) // sidenav 25%
                        .fillMaxHeight()
                ) {
                    SideNavigation(
                        navController = navController,
                        songPlayerViewModel = songPlayerViewModel,
                        modifier = Modifier.weight(1f)
                    )

                    if (currentSong != null && currentRoute != "track_view") {
                        MiniPlayer(
                            songPlayerViewModel,
                            onExpand = {
                                val currentScreen = currentRoute ?: NavigationItem.Home.route
                                Log.d("Navigation", "Storing current screen: $currentScreen")
                                songPlayerViewModel.setLastScreenRoute(currentScreen)
                                navController.navigate("track_view")
                            }
                        )
                    }
                }

                // buat main content
                Box(modifier = Modifier.weight(1f)) {
                    NavigationGraph(songPlayerViewModel, navController = navController)
                }
            }
        } else {
            // portrait awal
            Scaffold(
                bottomBar = {
                    Column {
                        if (currentSong != null && currentRoute != "track_view") {
                            MiniPlayer(
                                songPlayerViewModel,
                                onExpand = {
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
}

@Composable
fun NavigationGraph(songPlayerViewModel: SongPlayerViewModel, navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) {
            HomeFragment(
                songPlayerViewModel = songPlayerViewModel,
                onPlay = {
                    songPlayerViewModel.setLastScreenRoute(NavigationItem.Home.route)
                    navController.navigate("track_view")
                },
                onOnlineSong = { path ->
                    songPlayerViewModel.setLastScreenRoute(NavigationItem.Home.route)
                    if (path != null) {
                        navController.navigate(path)
                    }
                })
        }
        composable(NavigationItem.Library.route) {
            LibraryScreen(songPlayerViewModel, onPlay = {
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
        composable("online_song_global") {
            OnlineSongListScreen(
                songPlayerViewModel = songPlayerViewModel,
                isGlobal = true,
                onPlay = {
                    songPlayerViewModel.setLastScreenRoute("online_song_global")
                    navController.navigate("track_view")
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
        composable("online_song_country") {
            OnlineSongListScreen(
                songPlayerViewModel = songPlayerViewModel,
                isGlobal = false,
                onPlay = {
                    songPlayerViewModel.setLastScreenRoute("online_song_country")
                    navController.navigate("track_view")
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun SideNavigation(
    navController: NavHostController,
    songPlayerViewModel: SongPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Library,
        NavigationItem.Profile
    )

    NavigationRail(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isNonMainScreen = currentRoute != NavigationItem.Home.route &&
                currentRoute != NavigationItem.Library.route &&
                currentRoute != NavigationItem.Profile.route

        items.forEach { item ->
            NavigationRailItem(
                selected = currentRoute == item.route,
                onClick = {
                    Log.d("Navigation", "Clicked on: ${item.route}, current route: $currentRoute")
                    if (currentRoute == item.route) return@NavigationRailItem
                    if (isNonMainScreen) {
                        navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
                    }
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (currentRoute == item.route) {
                                    item.selectedIconResId
                                } else {
                                    item.iconResId
                                }
                            ),
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 12.dp),
                            color = if (currentRoute == item.route)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                label = null,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent,
                )
            )
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController, songPlayerViewModel: SongPlayerViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (!isLandscape) {
        val items = listOf(
            NavigationItem.Home,
            NavigationItem.Library,
            NavigationItem.Profile
        )

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val isNonMainScreen = currentRoute != NavigationItem.Home.route &&
                    currentRoute != NavigationItem.Library.route &&
                    currentRoute != NavigationItem.Profile.route

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
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 12.sp
                        )
                    },
                    selected = currentRoute == item.route,
                    onClick = {
                        Log.d("Navigation", "Clicked on: ${item.route}, current route: $currentRoute")
                        if (currentRoute == item.route) return@NavigationBarItem
                        if (isNonMainScreen) {
                            navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
                        }
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
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
}
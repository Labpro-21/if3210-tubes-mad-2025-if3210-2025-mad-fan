package itb.ac.id.purrytify.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import itb.ac.id.purrytify.ui.home.HomeFragment
//import itb.ac.id.purrytify.ui.library.LibraryFragment
import itb.ac.id.purrytify.ui.profile.ProfileFragment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import itb.ac.id.purrytify.ui.library.LibraryScreen
import itb.ac.id.purrytify.ui.profile.ProfileContent
import itb.ac.id.purrytify.ui.profile.ProfileUiState

@Composable
fun MainScreen(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
//        // Di desain figma ga ada header? kalo butuh nanti uncomment
//        topBar = {
//            currentRoute?.let { Header(currentRoute = it) }
//        },
        bottomBar = { BottomNavigation(navController = navController) }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            NavigationGraph(navController = navController)
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) {
            HomeFragment()
        }
        composable(NavigationItem.Library.route) {
            LibraryScreen()

        }
        composable(NavigationItem.Profile.route) {
//            Dummy untuk preview
            val dummyProfileState = ProfileUiState(
                username = "13522001",
                location = "Indonesia"
            )

            ProfileContent(profileState = dummyProfileState)

//            ProfileFragment()
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController) {
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
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
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
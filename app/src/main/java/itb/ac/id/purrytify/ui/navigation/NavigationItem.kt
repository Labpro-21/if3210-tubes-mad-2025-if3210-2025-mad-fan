package itb.ac.id.purrytify.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.vectorResource
import itb.ac.id.purrytify.R

sealed class NavigationItem(
    val route: String,
    val title: String,
    val iconResId: Int,
    val selectedIconResId: Int
) {
    object Home : NavigationItem(
        route = "home",
        title = "Home",
        iconResId = R.drawable.ic_home_outline,
        selectedIconResId = R.drawable.ic_home_filled
    )

    object Library : NavigationItem(
        route = "library",
        title = "Your Library",
        iconResId = R.drawable.ic_library_outline,
        selectedIconResId = R.drawable.ic_library_filled
    )

    object Profile : NavigationItem(
        route = "profile",
        title = "Profile",
        iconResId = R.drawable.ic_profile_outline,
        selectedIconResId = R.drawable.ic_profile_filled
    )
}
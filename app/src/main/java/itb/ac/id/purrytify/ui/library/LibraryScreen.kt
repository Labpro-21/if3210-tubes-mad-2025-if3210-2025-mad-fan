// LibraryScreen.kt
package itb.ac.id.purrytify.ui.library

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.remember
import androidx.core.view.ViewCompat.generateViewId
import itb.ac.id.purrytify.ui.home.HomeFragment
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme

@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fragmentManager = (context as AppCompatActivity).supportFragmentManager

    val viewId = remember { generateViewId() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                id = viewId
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
        update = { containerView ->
            val existingFragment = fragmentManager.findFragmentById(viewId)
            if (existingFragment == null) {
                fragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(viewId, LibraryFragment(), "LibraryFragment")
                }
            }
        }
    )
}


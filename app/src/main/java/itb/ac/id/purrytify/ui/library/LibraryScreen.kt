package itb.ac.id.purrytify.ui.library

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat.generateViewId
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.ui.addsong.AddSongScreen
import itb.ac.id.purrytify.ui.addsong.AddSongViewModel

@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fragmentManager = (context as AppCompatActivity).supportFragmentManager
    val viewId = remember { View.generateViewId() }
    val addSongViewModel: AddSongViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    var showSheet by remember { mutableStateOf(false) }
    PurrytifyTheme {
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showSheet = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add Song",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { ctx ->
                    FragmentContainerView(ctx).apply {
                        id = viewId
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
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
        Box() {
            if (showSheet) {
                AddSongScreen(
                    onDismiss = { showSheet = false },
                    onSave = { showSheet = false },
                    viewModel = addSongViewModel
                )
            }
        }
    }
}


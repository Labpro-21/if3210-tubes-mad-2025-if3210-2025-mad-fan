package itb.ac.id.purrytify.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import itb.ac.id.purrytify.service.NotificationService
import itb.ac.id.purrytify.ui.navigation.MainScreen
import itb.ac.id.purrytify.ui.player.SongPlayerViewModel
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme
import itb.ac.id.purrytify.utils.ConnectionStatus
import itb.ac.id.purrytify.utils.NetworkConnectivityObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.os.Build
import android.util.Log

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() // Biar aplikasi penuh sampe ke status bar
        super.onCreate(savedInstanceState)
        val deepLink = intent?.data
        setContent {
            PurrytifyTheme {
                PurrytifyApp(deepLink)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurrytifyApp(deeplink: Uri? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val networkStatus by connectivityObserver.observe().collectAsState(initial = ConnectionStatus.Available)
    val songPlayerViewModel = hiltViewModel<SongPlayerViewModel>()
    var showNoInternetSnackbar by remember { mutableStateOf(false) }
    var showRestoredSnackbar by remember { mutableStateOf(false) }
    var previousNetworkStatus by remember { mutableStateOf<ConnectionStatus?>(null) }

    // Snackbar berdasarkan network
    LaunchedEffect(networkStatus) {
        when (networkStatus) {
            ConnectionStatus.Available -> {
                if (previousNetworkStatus == ConnectionStatus.Lost ||
                    previousNetworkStatus == ConnectionStatus.Unavailable) {
                    showNoInternetSnackbar = false
                    showRestoredSnackbar = true
                    delay(3000) // Auto close untuk restored snacbar
                    showRestoredSnackbar = false
                }
            }
            ConnectionStatus.Unavailable, ConnectionStatus.Lost -> {
                showNoInternetSnackbar = true
                showRestoredSnackbar = false
            }
            else -> {}
        }
        previousNetworkStatus = networkStatus
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen(navController = navController, songPlayerViewModel = songPlayerViewModel, deeplink)
        }

        // Snackbar
        if (showNoInternetSnackbar) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                action = {
                    IconButton(onClick = { showNoInternetSnackbar = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.Black,
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ) {
                Text("No internet connection available")
            }
        }

        if (showRestoredSnackbar) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                action = {
                    IconButton(onClick = { showRestoredSnackbar = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.Black,
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ) {
                Text("Internet connection restored")
            }
        }
    }
}
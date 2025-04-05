package itb.ac.id.purrytify.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.Surface
import itb.ac.id.purrytify.ui.home.HomeFragment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // Biar aplikasi penuh sampe ke status bar

        setContent {
            PurrytifyTheme {
                PurrytifyApp()
            }
        }
    }
}

@Composable
fun PurrytifyApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Sekarang home di sini dulu
        // nanti diganti sama navigasi realnya
        HomeFragment()
    }
}

@Preview(showBackground = true)
@Composable
fun PurrytifyAppPreview() {
    PurrytifyTheme {
        PurrytifyApp()
    }
}
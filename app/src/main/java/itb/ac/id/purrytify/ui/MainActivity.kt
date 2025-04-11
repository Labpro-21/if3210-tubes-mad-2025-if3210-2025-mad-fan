package itb.ac.id.purrytify.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import itb.ac.id.purrytify.ui.navigation.MainScreen
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() // Biar aplikasi penuh sampe ke status bar
        super.onCreate(savedInstanceState)
        setContent {
            PurrytifyTheme {
                PurrytifyApp()
            }
        }
    }
}

@Composable
fun PurrytifyApp() {
    val navController = rememberNavController()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        MainScreen(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun PurrytifyAppPreview() {
    PurrytifyTheme {
        PurrytifyApp()
    }
}
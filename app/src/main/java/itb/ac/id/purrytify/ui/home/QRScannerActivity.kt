package itb.ac.id.purrytify.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dagger.hilt.android.AndroidEntryPoint
import itb.ac.id.purrytify.data.repository.OnlineSongRepository
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme
import kotlinx.coroutines.launch
import javax.inject.Inject
import itb.ac.id.purrytify.ui.MainActivity

@AndroidEntryPoint
class QRScannerActivity : ComponentActivity() {

    @Inject
    lateinit var onlineSongRepository: OnlineSongRepository

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startQRScanner()
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PurrytifyTheme {
                QRScannerScreen(
                    onBackPressed = { finish() },
                    onScanPressed = { checkCameraPermissionAndScan() }
                )
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startQRScanner()
        } else {
            checkCameraPermissionAndScan()
        }
    }

    private fun checkCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startQRScanner()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR Code for Purrytify Song")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result.contents != null) {
            val scannedContent = result.contents
            Log.d("QRScanner", "Scanned content: $scannedContent")

            // validate link
            if (isValidPurrytifyQR(scannedContent)) {
                val songId = extractSongIdFromDeepLink(scannedContent)
                if (songId != null) {
                    validateAndPlaySong(songId)
                } else {
                    showErrorAndFinish("Invalid QR code format")
                }
            } else {
                showErrorAndFinish("This QR code is not from Purrytify app")
            }
        } else {
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun isValidPurrytifyQR(content: String): Boolean {
        return content.startsWith("purrytify://song/")
    }

    private fun extractSongIdFromDeepLink(deepLink: String): String? {
        return try {
            val uri = Uri.parse(deepLink)
            uri.lastPathSegment
        } catch (e: Exception) {
            Log.e("QRScanner", "Error parsing deep link: ${e.message}")
            null
        }
    }

    private fun validateAndPlaySong(songId: String) {
        lifecycleScope.launch {
            try {
                val song = onlineSongRepository.getOnlineSongById(songId)
                if (song != null) {
                    // redirect MainActivity deeplinknya
                    val intent = Intent(this@QRScannerActivity, MainActivity::class.java).apply {
                        data = Uri.parse("purrytify://song/$songId")
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    showErrorAndFinish("Song not found or no longer available")
                }
            } catch (e: Exception) {
                Log.e("QRScanner", "Error validating song: ${e.message}")
                showErrorAndFinish("Error connecting to server. Please check your internet connection.")
            }
        }
    }

    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onBackPressed: () -> Unit,
    onScanPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Preparing camera...",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Point your camera at a Purrytify QR code to scan and play the song",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = onScanPressed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Scanning")
            }
        }
    }
}
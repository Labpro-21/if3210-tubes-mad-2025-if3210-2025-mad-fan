package itb.ac.id.purrytify.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.HttpURLConnection
import java.net.URL

class MapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", 0))

        setContent {
            MapLocationPickerScreen(
                onLocationSelected = { countryCode, countryName ->
                    val resultIntent = Intent().apply {
                        putExtra("country_code", countryCode)
                        putExtra("country_name", countryName)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                },
                onBack = {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLocationPickerScreen(
    onLocationSelected: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedCountryName by remember { mutableStateOf<String?>(null) }
    var selectedCountryCode by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Select Location on Map") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (selectedLocation != null && selectedCountryName != null) {
                    IconButton(
                        onClick = {
                            selectedCountryCode?.let { code ->
                                selectedCountryName?.let { name ->
                                    onLocationSelected(code, name)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm")
                    }
                }
            }
        )

        // map
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(3.0)
                        controller.setCenter(GeoPoint(0.0, 0.0))

                        setOnTouchListener { view, event ->
                            when (event.action) {
                                android.view.MotionEvent.ACTION_DOWN -> {
                                    view.requestFocus()
                                    view.parent.requestDisallowInterceptTouchEvent(true)
                                }
                                android.view.MotionEvent.ACTION_UP -> {
                                    // tap location
                                    val projection = projection
                                    val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint

                                    overlays.clear()

                                    val marker = Marker(this).apply {
                                        position = geoPoint
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        title = "Selected Location"
                                    }
                                    overlays.add(marker)
                                    invalidate()

                                    selectedLocation = geoPoint

                                    coroutineScope.launch {
                                        isLoading = true
                                        try {
                                            val countryInfo = getCountryFromCoordinates(geoPoint.latitude, geoPoint.longitude)
                                            selectedCountryName = countryInfo.first
                                            selectedCountryCode = countryInfo.second
                                        } catch (e: Exception) {
                                            Log.e("MapPicker", "Error getting country info", e)
                                            Toast.makeText(context, "Failed to get location info", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isLoading = false
                                        }
                                    }

                                    view.parent.requestDisallowInterceptTouchEvent(false)
                                }
                                android.view.MotionEvent.ACTION_CANCEL -> {
                                    view.parent.requestDisallowInterceptTouchEvent(false)
                                }
                            }
                            false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // info
        if (selectedLocation != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Selected Location",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Latitude: ${String.format("%.6f", selectedLocation!!.latitude)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Longitude: ${String.format("%.6f", selectedLocation!!.longitude)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (selectedCountryName != null) {
                        Text(
                            text = "Country: $selectedCountryName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Tap on the map to select a location",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

suspend fun getCountryFromCoordinates(latitude: Double, longitude: Double): Pair<String, String> {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=3&addressdetails=1"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "PurrytifyApp/1.0")

            val response = connection.inputStream.bufferedReader().readText()
            val jsonResponse = org.json.JSONObject(response)

            val address = jsonResponse.optJSONObject("address")
            val countryName = address?.optString("country") ?: "Indonesia"
            val countryCode = address?.optString("country_code")?.uppercase() ?: "ID"

            Pair(countryName, countryCode)
        } catch (e: Exception) {
            Log.e("MapPicker", "Error in reverse geocoding", e)
            Pair("Unknown", "XX")
        }
    }
}
package itb.ac.id.purrytify.ui.onlinesong

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.model.OnlineSongResponse
import itb.ac.id.purrytify.data.model.toSong
import itb.ac.id.purrytify.ui.player.SongPlayerViewModel
import itb.ac.id.purrytify.ui.profile.NoInternetScreen
import itb.ac.id.purrytify.utils.OnlineSongUtil
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.CreateQRModalBottomSheet
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.generateQRBitmap
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.shareDeepLink
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun OnlineSongListScreen(
    onlineSongViewModel: OnlineSongViewModel = hiltViewModel(),
    songPlayerViewModel: SongPlayerViewModel,
    isGlobal: Boolean,
    onPlay: () -> Unit,
    onBackPressed: () -> Unit = {}
) {
    val songs = onlineSongViewModel.onlineSongs.collectAsState(emptyList())
    val selectedCountryCode = onlineSongViewModel.location.collectAsState()
    val isLoading = onlineSongViewModel.isLoading
    val isNetworkAvailable = onlineSongViewModel.networkAvailable.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val gradientColors = if (isGlobal) {
        listOf(
            Color(0xFF1A7B72), // Teal
            Color(0xFF1E3264)  // Dark Teal
        )
    } else {
        listOf(
            Color(0xFFFF6B6B), // Pink Red
            Color(0xFFEC1E32)  // Red
        )
    }

    // Show no internet screen if network is not available
    if (!isNetworkAvailable.value) {
        NoInternetScreen(onRetryClick = {
            if (isGlobal) {
                onlineSongViewModel.fetchOnlineSongsGlobal()
            } else {
                onlineSongViewModel.fetchOnlineSongsCountry(selectedCountryCode.value)
            }
        })
        return
    }

    // Fetch songs when selectedCountryCode changes
    LaunchedEffect(isGlobal, selectedCountryCode.value) {
        if (isNetworkAvailable.value) {
            Log.d("Network", "Network available fetching song: ${isNetworkAvailable.value}")
            Log.d("OnlineSongListScreen", "Fetching online songs")
            if (isGlobal) {
                onlineSongViewModel.fetchOnlineSongsGlobal()
            } else {
                Log.d("OnlineSongListScreen", "Fetching online songs for country: ${selectedCountryCode.value}")
                onlineSongViewModel.fetchOnlineSongsCountry(selectedCountryCode.value)
            }
        } else {
            Log.d("OnlineSongListScreen", "Network is not available, skipping fetch")
        }
    }

    // landscape
    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors + MaterialTheme.colorScheme.background,
                        startY = 0f,
                        endY = 1200f
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = gradientColors + MaterialTheme.colorScheme.background,
                            startY = 0f,
                            endY = 1200f
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        // country selector landscape
//                        if (!isGlobal) {
//                            CountrySelector(
//                                selectedCountry = selectedCountryCode,
//                                onCountrySelected = { newCode ->
//                                    selectedCountryCode = newCode
//                                }
//                            )
//                        } else {
//                            Spacer(modifier = Modifier.width(1.dp))
//                        }
                        IconButton(
                            onClick = {
                                songs.value.forEach { song ->
                                    onlineSongViewModel.downloadSong(context, song.toSong())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download All Songs",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // album art kiri
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = gradientColors.map { it.copy(alpha = 0.8f) }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isGlobal) "Top 50" else "Top 10",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(2.dp)
                                    .background(Color.White.copy(alpha = 0.6f))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isGlobal) "GLOBAL" else "COUNTRY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f),
                                letterSpacing = 3.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isGlobal)
                            "Your daily update of the most played tracks right now - Global."
                        else
                            "Your daily update of the most played tracks right now - ${
                                listOf(
                                    "Indonesia" to "ID",
                                    "Malaysia" to "MY",
                                    "United States" to "US",
                                    "United Kingdom" to "GB",
                                    "Switzerland" to "CH",
                                    "Germany" to "DE",
                                    "Brazil" to "BR"
                                ).find { it.second == selectedCountryCode.value }?.first ?: "Country"
                            }.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Purrytify",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

//                    if (!isGlobal) {
//                        Spacer(modifier = Modifier.height(24.dp))
//                        Column {
//                            Text(
//                                text = "Select Country",
//                                style = MaterialTheme.typography.titleSmall,
//                                color = Color.White,
//                                modifier = Modifier.padding(bottom = 8.dp)
//                            )
//                            CountrySelector(
//                                selectedCountry = selectedCountryCode,
//                                onCountrySelected = { newCode ->
//                                    selectedCountryCode = newCode
//                                }
//                            )
//                        }
//                    }
                }
            }

            // list song kanan
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = gradientColors[0]
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        if (songs.value.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No songs available",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(songs.value) { index, song ->
                                SongItem(
                                    song = song,
                                    songNumber = index + 1,
                                    songPlayerViewModel = songPlayerViewModel,
                                    onPlay = onPlay,
                                    onlineSongViewModel = onlineSongViewModel,
                                    backgroundColor = MaterialTheme.colorScheme.background
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // portrait awal
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors + MaterialTheme.colorScheme.background,
                        startY = 0f,
                        endY = 1200f
                    )
                )
        ) {
            // Header
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                songs.value.forEach { song ->
                                    onlineSongViewModel.downloadSong(context, song.toSong())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download All Songs",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Album Art
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = gradientColors.map { it.copy(alpha = 0.8f) }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isGlobal) "Top 50" else "Top 10",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(2.dp)
                                        .background(Color.White.copy(alpha = 0.6f))
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (isGlobal) "GLOBAL" else listOf(
                                        "INDONESIA" to "ID",
                                        "MALAYSIA" to "MY",
                                        "UNITED STATES" to "US",
                                        "UNITED KINGDOM" to "GB",
                                        "SWITZERLAND" to "CH",
                                        "GERMANY" to "DE",
                                        "BRAZIL" to "BR"
                                    ).find { it.second == selectedCountryCode.value }?.first ?: "Country",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    letterSpacing = 4.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isGlobal)
                                "Your daily update of the most played tracks right now - Global."
                            else
                                "Your daily update of the most played tracks right now - ${
                                    listOf(
                                        "Indonesia" to "ID",
                                        "Malaysia" to "MY",
                                        "United States" to "US",
                                        "United Kingdom" to "GB",
                                        "Switzerland" to "CH",
                                        "Germany" to "DE",
                                        "Brazil" to "BR"
                                    ).find { it.second == selectedCountryCode.value }?.first ?: "Country"
                                }.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Purrytify",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
//                    if (!isGlobal) {
//                        Row(
//                            modifier = Modifier
//                                .padding(horizontal = 16.dp, vertical = 8.dp)
//                                .fillMaxWidth(),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = "Select Country",
//                                style = MaterialTheme.typography.titleMedium,
//                                color = Color.White,
//                                modifier = Modifier.weight(1f)
//                            )
//                            CountrySelector(
//                                selectedCountry = selectedCountryCode,
//                                onCountrySelected = { newCode ->
//                                    selectedCountryCode = newCode
//                                }
//                            )
//                        }
//                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = gradientColors[0]
                        )
                    }
                }
            } else {
                if (songs.value.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No songs available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }else {
                    itemsIndexed(songs.value) { index, song ->
                        SongItem(
                            song = song,
                            songNumber = index + 1,
                            songPlayerViewModel = songPlayerViewModel,
                            onPlay = onPlay,
                            onlineSongViewModel = onlineSongViewModel,
                            backgroundColor = MaterialTheme.colorScheme.background
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountrySelector(
    selectedCountry: String,
    onCountrySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val countries = listOf(
        "Indonesia" to "ID",
        "Malaysia" to "MY",
        "United States" to "US",
        "United Kingdom" to "GB",
        "Switzerland" to "CH",
        "Germany" to "DE",
        "Brazil" to "BR"
    )

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Text(
                text = countries.find { it.second == selectedCountry }?.first ?: "Select Country",
                color = Color.White
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            countries.forEach { (name, code) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onCountrySelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SongItem(
    song: OnlineSongResponse,
    songNumber: Int,
    songPlayerViewModel: SongPlayerViewModel,
    onPlay: () -> Unit,
    onlineSongViewModel: OnlineSongViewModel,
    backgroundColor: Color = MaterialTheme.colorScheme.background
) {
    val context = LocalContext.current
    val showQRSheet = remember { mutableStateOf(false) }
    val showShareMenu = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable {
                songPlayerViewModel.playSong(song.toSong())
                onPlay()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = songNumber.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(32.dp)
        )

        Spacer(modifier = Modifier.width(2.dp))

        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }

        Row {
            IconButton(
                onClick = { onlineSongViewModel.downloadSong(context, song.toSong()) }
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Share
            Box {
                IconButton(onClick = { showShareMenu.value = true }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showShareMenu.value,
                    onDismissRequest = { showShareMenu.value = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    // URL
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Share URL",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share via URL")
                            }
                        },
                        onClick = {
                            shareDeepLink(context, "purrytify://song/" + song.id)
                            showShareMenu.value = false
                        }
                    )
                    // QR
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "Share QR",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share via QR")
                            }
                        },
                        onClick = {
                            showQRSheet.value = true
                            showShareMenu.value = false
                        }
                    )
                }
            }
        }

        if (showQRSheet.value) {
            CreateQRModalBottomSheet(
                context = context,
                title = song.title,
                artist = song.artist,
                deepLink = "purrytify://song/${song.id}",
                onDismiss = { showQRSheet.value = false }
            )
        }
    }
}
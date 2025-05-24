package itb.ac.id.purrytify.ui.onlinesong

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import itb.ac.id.purrytify.data.model.OnlineSongResponse
import itb.ac.id.purrytify.data.model.toSong
import itb.ac.id.purrytify.ui.player.SongPlayerViewModel
import itb.ac.id.purrytify.utils.OnlineSongUtil
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.CreateQRModalBottomSheet
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.generateQRBitmap
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.shareDeepLink

@Composable
fun OnlineSongListScreen(
    onlineSongViewModel: OnlineSongViewModel = hiltViewModel(),
    songPlayerViewModel: SongPlayerViewModel,
    isGlobal: Boolean,
    onPlay: () -> Unit
) {
    val songs = onlineSongViewModel.onlineSongs.collectAsState(emptyList())
    var selectedCountryCode by remember { mutableStateOf("ID") }
    val isLoading = onlineSongViewModel.isLoading
    val context = LocalContext.current
    // Fetch songs when selectedCountryCode changes
    LaunchedEffect(isGlobal, selectedCountryCode) {
        if (isGlobal) {
            onlineSongViewModel.fetchOnlineSongsGlobal()
        } else {
            onlineSongViewModel.fetchOnlineSongsCountry(selectedCountryCode)
        }
    }
    Column {
        if (!isGlobal) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Country",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                CountrySelector(
                    selectedCountry = selectedCountryCode,
                    onCountrySelected = { newCode ->
                        selectedCountryCode = newCode
                    }
                )
                IconButton(onClick = {
                    songs.value.forEach { song ->
                        onlineSongViewModel.downloadSong(context, song.toSong())
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download All Songs",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Global Songs",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    songs.value.forEach { song ->
                        onlineSongViewModel.downloadSong(context, song.toSong())
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download All Songs",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        } else {
            SongList(songs.value,
                songPlayerViewModel = songPlayerViewModel,
                onPlay = onPlay,
                onlineSongViewModel = onlineSongViewModel
            )
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
            onClick = { expanded = true }
        ) {
            Text(text = countries.find { it.second == selectedCountry }?.first ?: "Select Country")
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
fun SongList(songs: List<OnlineSongResponse>, songPlayerViewModel: SongPlayerViewModel, onPlay: () -> Unit, onlineSongViewModel: OnlineSongViewModel) {
    LazyColumn {
        items(songs) { song ->
            SongItem(
                song,
                songPlayerViewModel = songPlayerViewModel,
                onPlay = onPlay,
                onlineSongViewModel
            )
        }
    }
}

@Composable
fun SongItem(song: OnlineSongResponse, songPlayerViewModel: SongPlayerViewModel, onPlay: () -> Unit, onlineSongViewModel: OnlineSongViewModel) {
    val context = LocalContext.current
    val showQRSheet = remember { mutableStateOf(false) }
    val showShareMenu = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                songPlayerViewModel.playSong(song.toSong())
                onPlay() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(song.title, style = MaterialTheme.typography.bodyLarge)
            Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onlineSongViewModel.downloadSong(context, song.toSong()) }) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // share jadi dropdown option
        Box {
            IconButton(onClick = { showShareMenu.value = true }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onPrimary
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
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
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
                                tint = MaterialTheme.colorScheme.onPrimary,
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
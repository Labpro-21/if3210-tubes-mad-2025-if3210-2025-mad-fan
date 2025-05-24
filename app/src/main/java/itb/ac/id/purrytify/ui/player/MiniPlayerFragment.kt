package itb.ac.id.purrytify.ui.player

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.ui.onlinesong.OnlineSongViewModel
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.CreateQRModalBottomSheet
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.shareDeepLink

@Composable
fun MiniPlayer(viewModel: SongPlayerViewModel, onExpand: () -> Unit) {
    val currentSong by viewModel.currentSong.collectAsState()
    val position by viewModel.position.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val onlineSongViewModel = hiltViewModel<OnlineSongViewModel>()
    val context = LocalContext.current
    val deepLink = "purrytify://song/${currentSong?.songId}"

    val showQRSheet = remember { mutableStateOf(false) }
    val showShareMenu = remember { mutableStateOf(false) }

    if (currentSong != null) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onExpand() },
            color = Color(0x8e550a1c),
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = currentSong!!.imagePath,
                        contentDescription = "Artwork",
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Song title and artist
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(currentSong!!.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(currentSong!!.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    }

                    // share jadi dropdown option
                    if (currentSong!!.isOnline) {
                        Box {
                            IconButton(onClick = { showShareMenu.value = true }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    tint = Color.White,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showShareMenu.value,
                                onDismissRequest = { showShareMenu.value = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = "Share URL",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Share via URL")
                                        }
                                    },
                                    onClick = {
                                        shareDeepLink(context, deepLink)
                                        showShareMenu.value = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode2,
                                                contentDescription = "Share QR",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
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

                    IconButton(onClick = { viewModel.previousSong() }) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        viewModel.togglePlayPause()
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            tint = Color.White,
                            contentDescription = "Play/Pause"
                        )
                    }
                    IconButton(onClick = { viewModel.nextSong() }) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White
                        )
                    }
                }
                // Placeholder for progress bar
                LinearProgressIndicator(
                    progress = { position.toFloat() / currentSong!!.duration },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(0.dp))
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface,
                    drawStopIndicator = {},
                    gapSize = 0.dp,
                )
            }
        }

        if (showQRSheet.value) {
            CreateQRModalBottomSheet(
                context = context,
                title = currentSong!!.title,
                artist = currentSong!!.artist,
                deepLink = deepLink,
                onDismiss = { showQRSheet.value = false }
            )
        }
    }
}

@Composable
@Preview
fun MiniPlayerPreview() {
    val currentSong = Song(
        title = "Song Title",
        artist = "Artist Name",
        duration = 180,
        filePath = "path/to/song.mp3",
        songId = 1,
        imagePath = "",
        userID = 90
    )

    PurrytifyTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
    //            .clickable { onExpand() }
            color = Color(0x8e550a1c),

        ) {
            Column {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder for album art
                    Image(
                        painter = painterResource(id = R.drawable.cover_starboy),
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Song title and artist
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(currentSong.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(currentSong.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                    IconButton(onClick = {
                        //                viewModel.togglePlayPause()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            tint = Color.White,
                            contentDescription = "Play/Pause"
                        )
                    }
                }
                // Placeholder for progress bar
                LinearProgressIndicator(
                    progress = { 0.95f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(0.dp))
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface,
                    drawStopIndicator = {},
                    gapSize = 0.dp,
                )
            }

        }
    }
}

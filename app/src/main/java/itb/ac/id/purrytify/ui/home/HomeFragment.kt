package itb.ac.id.purrytify.ui.home

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.ui.player.SongPlayerViewModel
import itb.ac.id.purrytify.ui.onlinesong.OnlineSongViewModel

fun getCountryName(countryCode: String): String {
    val countryMap = mapOf(
        "ID" to "Indonesia",
        "MY" to "Malaysia",
        "US" to "United States",
        "GB" to "United Kingdom",
        "CH" to "Switzerland",
        "DE" to "Germany",
        "BR" to "Brazil"
    )
    return countryMap[countryCode.uppercase()] ?: "Country"
}

@Composable
fun HomeFragment(
    viewModel: HomeViewModel = hiltViewModel(),
    songPlayerViewModel: SongPlayerViewModel,
    onlineSongViewModel: OnlineSongViewModel = hiltViewModel(),
    onPlay: () -> Unit,
    onOnlineSong: (path: String?) -> Unit
) {
    val newSongs by viewModel.newSongs.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val userLocation by onlineSongViewModel.location.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeContent(newSongs, recentlyPlayed, songPlayerViewModel, userLocation = userLocation, onPlay, onOnlineSong)
    }
}

@Composable
fun HomeContent(
    newSongsData: List<Song>,
    recentlyPlayedData: List<Song>,
    songPlayerViewModel: SongPlayerViewModel,
    userLocation: String,
    onPlay: () -> Unit,
    onOnlineSong: (path: String?) -> Unit
) {
    val context = LocalContext.current
    val currentCountry = if (userLocation.isNotEmpty()) getCountryName(userLocation) else "Country"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Online Songs",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )

            IconButton(
                onClick = {
                    val intent = Intent(context, QRScannerActivity::class.java)
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan QR Code",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.width(120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnlineCover(
                    title = "Top 50",
                    subtitle = "Global",
                    gradientType = "global",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    onOnlineSong("online_song_global")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Global",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier.width(120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnlineCover(
                    title = "Top 10",
                    subtitle = currentCountry,
                    gradientType = "country",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    onOnlineSong("online_song_country")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentCountry,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "New songs",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // New Songs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(newSongsData) { song ->
                NewSongItem(song, onClick = {
                    songPlayerViewModel.playSong(it)
                    onPlay()
                })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Recently played",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Recently played
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            recentlyPlayedData.forEach { song ->
                RecentlyPlayedItem(song, onClick = {
                    songPlayerViewModel.playSong(it)
                    onPlay()
                })
            }
        }

        // Buat miniplayer & navigation nanti
        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
fun NewSongItem(song: Song, onClick: (Song) -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick(song) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cover
        Image(
            painter = rememberAsyncImagePainter(model = song.imagePath),
            contentDescription = "${song.title} cover",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Judul
        Text(
            text = song.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Artis
        Text(
            text = song.artist,
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RecentlyPlayedItem(song: Song, onClick: (Song) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(song) }
    ) {
        // Cover
        Image(
            painter = rememberAsyncImagePainter(model = song.imagePath),
            contentDescription = "${song.title} cover",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Judul
            Text(
                text = song.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Artis
            Text(
                text = song.artist,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
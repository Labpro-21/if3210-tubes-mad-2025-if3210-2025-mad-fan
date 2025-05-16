package itb.ac.id.purrytify.ui.onlinesong

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import itb.ac.id.purrytify.data.model.OnlineSongResponse
import itb.ac.id.purrytify.data.model.toSong
import itb.ac.id.purrytify.ui.player.SongPlayerViewModel

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
                onPlay = onPlay
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
        "United States" to "US",
        "Japan" to "JP",
        "India" to "IN",
        "Germany" to "DE"
    )

    Box {
        OutlinedButton(
            onClick = { expanded = true }
        ) {
            Text(text = countries.find { it.second == selectedCountry }?.first ?: "Select Country")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
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
fun SongList(songs: List<OnlineSongResponse>, songPlayerViewModel: SongPlayerViewModel, onPlay: () -> Unit) {
    LazyColumn {
        items(songs) { song ->
            SongItem(
                song,
                songPlayerViewModel = songPlayerViewModel,
                onPlay = onPlay
            )
        }
    }
}

@Composable
fun SongItem(song: OnlineSongResponse, songPlayerViewModel: SongPlayerViewModel, onPlay: () -> Unit) {
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
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(song.title, style = MaterialTheme.typography.bodyLarge)
            Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

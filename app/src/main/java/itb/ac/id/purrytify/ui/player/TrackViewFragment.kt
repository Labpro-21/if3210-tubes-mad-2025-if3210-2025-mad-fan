package itb.ac.id.purrytify.ui.player

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.ui.editsong.EditSongScreen
import itb.ac.id.purrytify.ui.editsong.EditSongViewModel
import itb.ac.id.purrytify.ui.onlinesong.OnlineSongViewModel
import itb.ac.id.purrytify.ui.theme.DavyGrey
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.CreateQRModalBottomSheet
import itb.ac.id.purrytify.utils.OnlineSongUtil.Companion.shareDeepLink
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun TrackViewFragment(
    viewModel: SongPlayerViewModel,
    editSongViewModel: EditSongViewModel = hiltViewModel(),
    onlineSongViewModel: OnlineSongViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.position.collectAsState()
    val ended by viewModel.isQueueEmpty.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    // Shuffle & repeat state
    val isShuffleActive by viewModel.isShuffleEnabled.collectAsState()
    // Mode repeat: 0 = Off, 1 = Repeat One, 2 = Repeat All
    val repeatMode by viewModel.repeatMode.collectAsState()

    val context = LocalContext.current
    var showSheetQR by remember { mutableStateOf(false) }

    // get config untuk orientasi
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(song) {
        Log.d("SongPlayer", "Song: ${viewModel.currentSong.value}")
    }

    val deepLink = "purrytify://song/${song?.songId}"

    LaunchedEffect(ended) {
        Log.d("SongPlayer", "ended: $ended")
        if (ended) {
            onBack()
        }
    }

    if (song != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            // lanscape
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // album kiri
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = song!!.imagePath),
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxSize()
                        )
                        if (isBuffering) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White,
                                strokeWidth = 4.dp
                            )
                        }
                    }

                    // music control kanan
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.ExpandMore,
                                    contentDescription = "Back",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .rotate(90f),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(DavyGrey)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Song") },
                                        onClick = {
                                            showMenu = false
                                            showSheet = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Song") },
                                        onClick = {
                                            showMenu = false
                                            viewModel.deleteSong()
                                            onBack()
                                        }
                                    )
                                    if (song!!.isOnline) {
                                        DropdownMenuItem(
                                            text = { Text("Download Song") },
                                            onClick = {
                                                showMenu = false
                                                onlineSongViewModel.downloadSong(context, song!!)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Share Song") },
                                            onClick = {
                                                showMenu = false
                                                shareDeepLink(context, deepLink)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Share Song QR") },
                                            onClick = {
                                                showMenu = false
                                                showSheetQR = true
                                            }
                                        )
                                    }
                                }
                                if (showSheetQR) {
                                    CreateQRModalBottomSheet(
                                        context = context,
                                        title = song!!.title,
                                        artist = song!!.artist,
                                        deepLink = deepLink,
                                        onDismiss = { showSheetQR = false }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song!!.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = song!!.artist,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            if (!song!!.isOnline) {
                                IconButton(onClick = viewModel::toggleFavorite) {
                                    Icon(
                                        imageVector = if (song!!.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        // slider durasi
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Slider(
                                value = position.toFloat() / song!!.duration.toFloat(),
                                onValueChange = { newValue ->
                                    viewModel.seekTo((song!!.duration * newValue).toLong())
                                },
                                onValueChangeFinished = {},
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.onSurface,
                                    activeTrackColor = MaterialTheme.colorScheme.onSurface,
                                    inactiveTrackColor = Color(0xff777777),
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = viewModel.formatTime(position),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = viewModel.formatTime(song!!.duration),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.toggleShuffle()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (isShuffleActive) Color.White else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            IconButton(onClick = { viewModel.previousSong() }) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(onClick = {
                                viewModel.togglePlayPause()
                            }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(onClick = { viewModel.nextSong() }) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.toggleRepeatMode()
                                }
                            ) {
                                Icon(
                                    imageVector = when (repeatMode) {
                                        SongPlayerViewModel.RepeatMode.OFF -> Icons.Default.Repeat
                                        SongPlayerViewModel.RepeatMode.ONE -> Icons.Default.RepeatOne
                                        SongPlayerViewModel.RepeatMode.ALL -> Icons.Default.Repeat
                                    },
                                    contentDescription = "Repeat",
                                    tint = when (repeatMode) {
                                        SongPlayerViewModel.RepeatMode.OFF -> Color.Gray
                                        SongPlayerViewModel.RepeatMode.ONE, SongPlayerViewModel.RepeatMode.ALL -> Color.White
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // portrait awal
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ExpandMore,
                                contentDescription = "Back",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(DavyGrey)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Song") },
                                    onClick = {
                                        showMenu = false
                                        showSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Song") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.deleteSong()
                                        onBack()
                                    }
                                )
                                if (song!!.isOnline) {
                                    DropdownMenuItem(
                                        text = { Text("Download Song") },
                                        onClick = {
                                            showMenu = false
                                            onlineSongViewModel.downloadSong(context, song!!)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share Song") },
                                        onClick = {
                                            showMenu = false
                                            shareDeepLink(context, deepLink)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share Song QR") },
                                        onClick = {
                                            showMenu = false
                                            showSheetQR = true
                                        }
                                    )
                                }
                            }
                            if (showSheetQR) {
                                CreateQRModalBottomSheet(
                                    context = context,
                                    title = song!!.title,
                                    artist = song!!.artist,
                                    deepLink = deepLink,
                                    onDismiss = { showSheetQR = false }
                                )
                            }
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(400.dp)
                            .padding(16.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = song!!.imagePath),
                            contentDescription = "Album Art",
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isBuffering) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = Color.White,
                                strokeWidth = 4.dp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = song!!.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = song!!.artist,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        // like for local only
                        if (!song!!.isOnline) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = viewModel::toggleFavorite) {
                                    Icon(
                                        imageVector = if (song!!.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Slider(
                        value = position.toFloat() / song!!.duration.toFloat(),
                        onValueChange = { newValue ->
                            viewModel.seekTo((song!!.duration * newValue).toLong())
                        },
                        onValueChangeFinished = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onSurface,
                            activeTrackColor = MaterialTheme.colorScheme.onSurface,
                            inactiveTrackColor = Color(0xff777777),
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.formatTime(position),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = viewModel.formatTime(song!!.duration),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle button
                        IconButton(
                            onClick = {
                                viewModel.toggleShuffle()
                            }
                        ) {
                            Icon(
                                Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffleActive) Color.White else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(onClick = { viewModel.previousSong() }) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        IconButton(onClick = {
                            viewModel.togglePlayPause()
                        }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.nextSong() }) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        // Repeat One/All button
                        IconButton(
                            onClick = {
                                // Cycle mode nya
                                viewModel.toggleRepeatMode()
                            }
                        ) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    SongPlayerViewModel.RepeatMode.OFF -> Icons.Default.Repeat
                                    SongPlayerViewModel.RepeatMode.ONE -> Icons.Default.RepeatOne
                                    SongPlayerViewModel.RepeatMode.ALL -> Icons.Default.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = when (repeatMode) {
                                    SongPlayerViewModel.RepeatMode.OFF -> Color.Gray
                                    SongPlayerViewModel.RepeatMode.ONE, SongPlayerViewModel.RepeatMode.ALL -> Color.White
                                },
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
        Box {
            if (showSheet) {
                EditSongScreen(
                    onDismiss = { showSheet = false },
                    onSave = { showSheet = false },
                    viewModel = editSongViewModel,
                    song = song!!,
                )
            }
        }
    }
}

@Composable
@Preview
fun TrackViewFragmentPreview() {
    val song = Song(
        songId = 1,
        title = "Song Title",
        artist = "Artist Name",
        filePath = "file_path",
        imagePath = "image_path",
        duration = 300000,
        userID = 1
    )
    val position = 150000L
    PurrytifyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row {
                    IconButton(onClick = { /* TODO: Back */ }) {
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = "Back",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { /* TODO: More options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.cover_starboy),
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .size(400.dp)
                        .padding(16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = song.artist,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Slider(
                    value = 0.5f,
                    onValueChange = { /* TODO: Seek to new position */ },
                    onValueChangeFinished = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.onSurface,
                        inactiveTrackColor = Color(0xff777777),
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${position / 1000 / 60}:${(position / 1000) % 60}",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${song.duration / 1000 / 60}:${(song.duration / 1000) % 60}",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { /* TODO: Previous song */ }) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(onClick = {
                        //                viewModel.togglePlayPause()
                    }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(onClick = { /* TODO: Next song */ }) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}
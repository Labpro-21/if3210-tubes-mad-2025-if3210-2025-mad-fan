package itb.ac.id.purrytify.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.entity.SongPlayCount
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

data class TopSong(
    val rank: Int,
    val title: String,
    val artist: String,
    val albumName: String,
    val plays: Int,
    val listeningTimeMinutes: Long,
    val imageId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSongsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SoundCapsuleViewModel = hiltViewModel()
) {
    val analyticsState by viewModel.analyticsState.collectAsState()
    val currentMonth = YearMonth.now()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    
    val topSongs = remember(analyticsState.topSongs) {
        analyticsState.topSongs.mapIndexed { index, songPlayCount ->
            TopSong(
                rank = index + 1,
                title = songPlayCount.songTitle,
                artist = songPlayCount.songArtist,
                albumName = "", // We don't have album info in our analytics
                plays = songPlayCount.playCount,
                listeningTimeMinutes = songPlayCount.totalListeningTime / 60,
                imageId = R.drawable.profile_dummy // Using placeholder image
            )
        }
    }
    
    val totalSongsPlayed = analyticsState.totalSongsPlayedThisMonth

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Top songs",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            )
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = currentMonth.format(monthFormatter),
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (analyticsState.isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFFFEB3B),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row {
                    Text(
                        text = "You played ",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                    Text(
                        text = "$totalSongsPlayed different songs",
                        color = Color(0xFFFFEB3B),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                }
                Text(
                    text = "this month.",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // list songs
        if (analyticsState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFFFEB3B),
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else if (topSongs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No songs played this month",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(topSongs) { index, song ->
                    Column {
                        TopSongItem(
                            song = song,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        if (index < topSongs.size - 1) {
                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopSongItem(
    song: TopSong,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // rank
        Text(
            text = String.format("%02d", song.rank),
            color = Color(0xFFFFEB3B),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            val subtitle = if (song.albumName.isNotEmpty()) {
                "${song.artist}, ${song.albumName}"
            } else {
                song.artist
            }

            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(
                    text = "${song.plays}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " plays • ",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = "${song.listeningTimeMinutes}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " min",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Image(
            painter = painterResource(id = song.imageId),
            contentDescription = "Album art for ${song.title}",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopSongsScreenPreview() {
    TopSongsScreen()
}
package itb.ac.id.purrytify.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import itb.ac.id.purrytify.R
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// buat streak info
data class StreakData(
    val days: Int,
    val songName: String,
    val artistName: String,
    val imageId: String?,
    val dateRange: String
)

@Composable
fun SoundCapsuleSection(
    modifier: Modifier = Modifier,
    onTimeListenedClick: () -> Unit = {},
    onTopArtistsClick: () -> Unit = {},
    onTopSongsClick: () -> Unit = {},
    onExportClick: () -> Unit = {}
) {
    val viewModel: SoundCapsuleViewModel = hiltViewModel()
    val analyticsUiState by viewModel.analyticsState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Sound Capsule",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onExportClick) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = "Download",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (analyticsUiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (analyticsUiState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Unable to load analytics: ${analyticsUiState.error}",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Check apakah empty atau ada data
            val hasData = analyticsUiState.totalListeningTimeThisMonth > 0 || 
                         analyticsUiState.topArtists.isNotEmpty() || 
                         analyticsUiState.topSongs.isNotEmpty()
            
            if (hasData) {
                // show data (jika ada data)
                val currentMonth = YearMonth.now()
                val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
                
                MonthlyCapsule(
                    month = currentMonth.format(monthFormatter),
                    minutesListened = analyticsUiState.totalListeningTimeThisMonth / 60,
                    topArtist = analyticsUiState.topArtists.firstOrNull()?.artist ?: "No data",
                    topArtistImageId = analyticsUiState.topArtists.firstOrNull()?.imagePath,
                    topSong = analyticsUiState.topSongs.firstOrNull()?.songTitle ?: "No data",
                    topSongImageId = analyticsUiState.topSongs.firstOrNull()?.imagePath,
                    hasStreak = analyticsUiState.dayStreaks.isNotEmpty(),
                    streakData = if (analyticsUiState.dayStreaks.isNotEmpty()) {
                        val firstStreak = analyticsUiState.dayStreaks.first()
                        StreakData(
                            days = firstStreak.streakDays,
                            songName = firstStreak.songTitle,
                            artistName = firstStreak.songArtist,
                            imageId = firstStreak.imagePath,
                            dateRange = "${firstStreak.startDate} - ${firstStreak.endDate}"
                        )
                    } else null,
                    onTimeListenedClick = onTimeListenedClick,
                    onTopArtistsClick = onTopArtistsClick,
                    onTopSongsClick = onTopSongsClick
                )
            } else {
                // show dummy data jika tidak ada data
                MonthlyCapsule(
                    month = "This Month",
                    minutesListened = 0,
                    topArtist = "Start listening to music",
                    topArtistImageId = "",
                    topSong = "No songs played yet",
                    topSongImageId = "",
                    hasStreak = false,
                    streakData = null,
                    onTimeListenedClick = onTimeListenedClick,
                    onTopArtistsClick = onTopArtistsClick,
                    onTopSongsClick = onTopSongsClick
                )
            }
        }
    }
}

@Composable
fun MonthlyCapsule(
    month: String,
    minutesListened: Long,
    topArtist: String,
    topArtistImageId: String?,
    topSong: String,
    topSongImageId: String?,
    hasStreak: Boolean = false,
    streakData: StreakData? = null,
    onTimeListenedClick: () -> Unit = {},
    onTopArtistsClick: () -> Unit = {},
    onTopSongsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = month,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTimeListenedClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time listened",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "View more",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "$minutesListened minutes",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // top artist
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTopArtistsClick() },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top artist",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = "View more",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = topArtist,
                        color = Color(0xFF2196F3),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(model = topArtistImageId ?: R.drawable.profile_dummy),
                        contentDescription = "Top Artist Cover",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // top song
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTopSongsClick() },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top song",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = "View more",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = topSong,
                        color = Color(0xFFFFEB3B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(model = topSongImageId),
                        contentDescription = "Top Song Cover",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // streak section
        if (hasStreak && streakData != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = streakData.imageId),
                            contentDescription = "Album Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "You had a ${streakData.days}-day streak",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // deskripsi
                    Text(
                        text = "You played ${streakData.songName} by ${streakData.artistName} day after day. You were on fire",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = streakData.dateRange,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share Streak",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
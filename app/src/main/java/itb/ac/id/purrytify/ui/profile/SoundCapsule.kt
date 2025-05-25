package itb.ac.id.purrytify.ui.profile

import android.util.Log
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

data class MonthlyDisplayData(
    val month: String,
    val displayMonth: String,
    val minutesListened: Long,
    val topArtist: String,
    val topArtistImageId: String?,
    val topSong: String,
    val topSongImageId: String?,
    val hasStreak: Boolean,
    val streakData: StreakData?
)

@Composable
fun SoundCapsuleSection(
    modifier: Modifier = Modifier,
    onTimeListenedClick: (String) -> Unit = {},
    onTopArtistsClick: (String) -> Unit = {},
    onTopSongsClick: (String) -> Unit = {},
    onExportClick: () -> Unit = {}
) {
    val viewModel: SoundCapsuleViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
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

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Unable to load analytics: ${uiState.error}",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Display all available monthly capsules
            val monthlyDisplayData = uiState.monthlyDisplayData
            
            // Add debug logging
            Log.d("SoundCapsule", "Monthly display data size: ${monthlyDisplayData.size}")
            Log.d("SoundCapsule", "Monthly history size: ${uiState.monthlyHistory.size}")
            
            if (monthlyDisplayData.isNotEmpty()) {
                // Show all monthly capsules, sorted by month descending (newest first)
                val sortedData = monthlyDisplayData.sortedByDescending { it.month }
                
                sortedData.forEach { monthlyData ->
                    Log.d("SoundCapsule", "Displaying month: ${monthlyData.month} with ${monthlyData.minutesListened} minutes")
                    MonthlyCapsule(
                        month = monthlyData.displayMonth,
                        monthId = monthlyData.month,
                        minutesListened = monthlyData.minutesListened,
                        topArtist = monthlyData.topArtist,
                        topArtistImageId = monthlyData.topArtistImageId,
                        topSong = monthlyData.topSong,
                        topSongImageId = monthlyData.topSongImageId,
                        hasStreak = monthlyData.hasStreak,
                        streakData = monthlyData.streakData,
                        onTimeListenedClick = { onTimeListenedClick(monthlyData.month) },
                        onTopArtistsClick = { onTopArtistsClick(monthlyData.month) },
                        onTopSongsClick = { onTopSongsClick(monthlyData.month) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // Show message when no data is available
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No listening data available",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Start listening to music to see your sound capsules!",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyCapsule(
    month: String,
    monthId: String? = null,
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
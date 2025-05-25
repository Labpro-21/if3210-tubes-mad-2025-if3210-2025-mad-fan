package itb.ac.id.purrytify.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

data class DailyListeningData(
    val day: Int,
    val minutes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeListenedScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SoundCapsuleViewModel = hiltViewModel()
) {
    val analyticsState by viewModel.analyticsState.collectAsState()
    val currentMonth = YearMonth.now()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    
    // map analytics data to menjadi UI
    val dailyData = remember(analyticsState.dailyListening) {
        analyticsState.dailyListening.map { daily ->
            DailyListeningData(
                day = try {
                    LocalDate.parse(daily.date).dayOfMonth
                } catch (e: Exception) {
                    daily.date.substringAfterLast("-").toIntOrNull() ?: 1
                },
                minutes = (daily.listeningTimeSeconds / 60).toInt()
            )
        }.sortedBy { it.day }
    }
    
    val totalMinutes = analyticsState.totalListeningTimeThisMonth / 60
    val averageMinutes = if (analyticsState.dailyListening.isNotEmpty()) {
        totalMinutes / analyticsState.dailyListening.size
    } else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Time listened",
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
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row {
                    Text(
                        text = "You listened to music for ",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                }
                Row {
                    Text(
                        text = "$totalMinutes minutes",
                        color = Color(0xFF4CAF50), // Green
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                    Text(
                        text = " this month.",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Daily average: $averageMinutes min",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Chart placeholder -- nanti pakai library charting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(300.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (analyticsState.isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (dailyData.isEmpty()) {
                    Text(
                        text = "No listening data available",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Column {
                        Text(
                            text = "Daily Listening Chart",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // chart placeholder
                        Text(
                            text = "Peak day: ${dailyData.maxByOrNull { it.minutes }?.let { "${it.day}th (${it.minutes} min)" } ?: "N/A"}",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Total days with listening: ${dailyData.count { it.minutes > 0 }}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun TimeListenedScreenPreview() {
    TimeListenedScreen()
}
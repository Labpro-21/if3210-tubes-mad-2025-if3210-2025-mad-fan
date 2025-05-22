package itb.ac.id.purrytify.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DailyListeningData(
    val day: Int,
    val minutes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeListenedScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Dummy data
    val dailyData = listOf(
        DailyListeningData(1, 45), DailyListeningData(2, 60), DailyListeningData(3, 30),
        DailyListeningData(4, 80), DailyListeningData(5, 25), DailyListeningData(6, 90),
        DailyListeningData(7, 55), DailyListeningData(8, 70), DailyListeningData(9, 40),
        DailyListeningData(10, 85), DailyListeningData(11, 35), DailyListeningData(12, 65),
        DailyListeningData(13, 50), DailyListeningData(14, 75), DailyListeningData(15, 20),
        DailyListeningData(16, 95), DailyListeningData(17, 60), DailyListeningData(18, 45),
        DailyListeningData(19, 80), DailyListeningData(20, 55), DailyListeningData(21, 70),
        DailyListeningData(22, 40), DailyListeningData(23, 85), DailyListeningData(24, 30),
        DailyListeningData(25, 90), DailyListeningData(26, 65), DailyListeningData(27, 50),
        DailyListeningData(28, 75), DailyListeningData(29, 55), DailyListeningData(30, 80)
    )

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
                text = "April 2025",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                    text = "862 minutes",
                    color = Color(0xFF4CAF50), // Green (materialTheme gabisa pake entah kenapa)
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
                text = "Daily average: 33 min",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // buat chart nanti
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
                Text(
                    text = "Daily Chart",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )

//                // Y-axis label
//                Text(
//                    text = "minutes",
//                    color = Color.Gray,
//                    fontSize = 12.sp,
//                    modifier = Modifier
//                        .align(Alignment.CenterStart)
//                        .padding(start = 8.dp)
//                )
//
//                // X-axis label
//                Text(
//                    text = "day",
//                    color = Color.Gray,
//                    fontSize = 12.sp,
//                    modifier = Modifier
//                        .align(Alignment.BottomEnd)
//                        .padding(end = 8.dp, bottom = 8.dp)
//                )
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
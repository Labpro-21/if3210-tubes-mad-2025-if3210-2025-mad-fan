package itb.ac.id.purrytify.ui.profile

import android.util.Log
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.max

data class DailyListeningData(
    val day: Int,
    val minutes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeListenedScreen(
    month: String = "",
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SoundCapsuleViewModel = hiltViewModel()
) {
    val analyticsState by viewModel.analyticsState.collectAsState()
    
    // Load analytics for the specific month
    LaunchedEffect(month) {
        if (month.isNotEmpty()) {
            viewModel.loadAnalyticsForMonth(month)
        }
    }
    
    // Parse month for display
    val displayMonth = remember(month) {
        if (month.isNotEmpty()) {
            viewModel.formatDisplayMonth(month)
        } else {
            val currentMonth = YearMonth.now()
            val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            currentMonth.format(monthFormatter)
        }
    }
    
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
                text = displayMonth,
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
                        
                        DailyListeningChart(
                            data = dailyData,
                            modifier = Modifier.fillMaxWidth(),
                            month = displayMonth
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Chart statistics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Peak day:",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = dailyData.maxByOrNull { it.minutes }?.let { "${it.day}th (${it.minutes} min)" } ?: "N/A",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Active days:",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${dailyData.count { it.minutes > 0 }}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun DailyListeningChart(
    data: List<DailyListeningData>,
    modifier: Modifier = Modifier,
    month: String
) {
    if (data.isEmpty()) return

    val maxMinutes = data.maxOfOrNull { it.minutes } ?: 1
    val minDay = data.minOfOrNull { it.day } ?: 1
    val maxDay = data.maxOfOrNull { it.day } ?: 31
    
    // handle kasus 1 hari
    val dayRange = if (maxDay == minDay) 10 else maxDay - minDay
    val adjustedMinDay = if (maxDay == minDay) maxOf(1, minDay - 5) else minDay
    val adjustedMaxDay = if (maxDay == minDay) minOf(31, maxDay + 5) else maxDay

    Column(modifier = modifier) {
        val textMeasurer = rememberTextMeasurer()
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val padding = 40.dp.toPx()
            val chartWidth = canvasWidth - 2 * padding
            val chartHeight = canvasHeight - 2 * padding

            // Draw axes
            val axisColor = Color.Gray
            val strokeWidth = 2.dp.toPx()

            // Y-axis (left)
            drawLine(
                color = axisColor,
                start = androidx.compose.ui.geometry.Offset(padding, padding),
                end = androidx.compose.ui.geometry.Offset(padding, canvasHeight - padding),
                strokeWidth = strokeWidth
            )

            // X-axis (bottom)
            drawLine(
                color = axisColor,
                start = androidx.compose.ui.geometry.Offset(padding, canvasHeight - padding),
                end = androidx.compose.ui.geometry.Offset(canvasWidth - padding, canvasHeight - padding),
                strokeWidth = strokeWidth
            )

            // Draw grid lines and Y-axis labels
            val gridColor = Color.Gray.copy(alpha = 0.3f)
            val gridStrokeWidth = 1.dp.toPx()

            // Y-axis grid lines (horizontal)
            val ySteps = 4
            for (i in 0..ySteps) {
                val y = canvasHeight - padding - (i * chartHeight / ySteps)
                
                // Draw grid line
                if (i > 0) {
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(padding, y),
                        end = androidx.compose.ui.geometry.Offset(canvasWidth - padding, y),
                        strokeWidth = gridStrokeWidth
                    )
                }
                
                // Y-axis labels (minutes)
                val minuteValue = (i * maxMinutes / ySteps).toInt()
                val textStyle = TextStyle(
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                val textLayoutResult = textMeasurer.measure(
                    text = "${minuteValue}m",
                    style = textStyle
                )
                
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = padding - textLayoutResult.size.width - 8.dp.toPx(),
                        y = y - textLayoutResult.size.height / 2
                    )
                )
            }

            // X-axis grid (vertical)
            for (i in 1..5) {
                val x = padding + (i * chartWidth / 5)
                drawLine(
                    color = gridColor,
                    start = androidx.compose.ui.geometry.Offset(x, padding),
                    end = androidx.compose.ui.geometry.Offset(x, canvasHeight - padding),
                    strokeWidth = gridStrokeWidth
                )
                
                // X-axis labels (days)
//                val dayValue = adjustedMinDay + (i * dayRange / 5)
//                val textStyle = TextStyle(
//                    color = Color.Gray,
//                    fontSize = 10.sp
//                )
//                val textLayoutResult = textMeasurer.measure(
//                    text = dayValue.toString(),
//                    style = textStyle
//                )
//
//                drawText(
//                    textLayoutResult = textLayoutResult,
//                    topLeft = androidx.compose.ui.geometry.Offset(
//                        x = x - textLayoutResult.size.width / 2,
//                        y = canvasHeight - padding + 8.dp.toPx()
//                    )
//                )
            }

            // Draw data points and lines
            val lineColor = Color(0xFF4CAF50)
            val pointColor = Color(0xFF4CAF50)
            val lineStrokeWidth = 3.dp.toPx()
            val pointRadius = 4.dp.toPx()

            val path = Path()
            var isFirstPoint = true

            data.forEach { point ->
                val x = padding + ((point.day - adjustedMinDay).toFloat() / dayRange.toFloat()) * chartWidth
                val y = canvasHeight - padding - (point.minutes.toFloat() / maxMinutes.toFloat()) * chartHeight

                if (data.size > 1) {
                    if (isFirstPoint) {
                        path.moveTo(x, y)
                        isFirstPoint = false
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Draw point
                drawCircle(
                    color = pointColor,
                    radius = pointRadius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }

            if (data.size > 1) {
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = lineStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$adjustedMinDay $month",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = "$adjustedMaxDay $month",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeListenedScreenPreview() {
    TimeListenedScreen()
}
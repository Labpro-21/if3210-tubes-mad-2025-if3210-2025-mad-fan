package itb.ac.id.purrytify.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.local.entity.*
import itb.ac.id.purrytify.data.repository.AnalyticsRepository
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val totalListeningTimeThisMonth: Long = 0L,
    val totalSongsPlayedThisMonth: Int = 0,
    val topArtists: List<ArtistPlayCount> = emptyList(),
    val topSongs: List<SongPlayCount> = emptyList(),
    val dailyListening: List<DailyListening> = emptyList(),
    val dayStreaks: List<DayStreak> = emptyList(),
    val error: String? = null
)

data class SoundCapsuleUiState(
    val isLoading: Boolean = false,
    val currentMonth: String = "",
    val totalListeningTime: Long = 0L,
    val dailyAverage: Long = 0L,
    val topArtist: String? = null,
    val topArtistPlayCount: Int = 0,
    val topSong: String? = null,
    val topSongArtist: String? = null,
    val topSongPlayCount: Int = 0,
    val dayStreaks: List<DayStreak> = emptyList(),
    val dailyStats: List<DailyStats> = emptyList(),
    val monthlyHistory: List<MonthlyAnalytics> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SoundCapsuleViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SoundCapsuleUiState())
    val uiState: StateFlow<SoundCapsuleUiState> = _uiState.asStateFlow()
    
    var isExporting by mutableStateOf(false)
        private set
    
    private val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    // New analytics state for the detail screens
    private val _analyticsState = MutableStateFlow(AnalyticsUiState())
    val analyticsState: StateFlow<AnalyticsUiState> = _analyticsState.asStateFlow()
    
    init {
        loadCurrentMonthAnalytics()
        loadAllAnalytics()
        loadDetailedAnalytics()
    }
    
    private fun loadCurrentMonthAnalytics() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val currentMonth = analyticsRepository.getCurrentMonthString()
                _uiState.value = _uiState.value.copy(currentMonth = currentMonth)
                
                // Combine multiple flows for real-time updates
                combine(
                    analyticsRepository.getTotalListeningTimeForMonth(currentMonth),
                    analyticsRepository.getTopArtistsForMonth(currentMonth, 1),
                    analyticsRepository.getTopSongsForMonth(currentMonth, 1),
                    analyticsRepository.getDayStreaksForMonth(currentMonth),
                    analyticsRepository.getDailyStatsForMonth(currentMonth)
                ) { totalTime, topArtists, topSongs, dayStreaks, dailyStats ->
                    
                    val topArtist = topArtists.firstOrNull()
                    val topSong = topSongs.firstOrNull()
                    val dailyAverage = if (dailyStats.isNotEmpty()) {
                        totalTime / dailyStats.size
                    } else 0L
                    
                    SoundCapsuleUiState(
                        isLoading = false,
                        currentMonth = currentMonth,
                        totalListeningTime = totalTime,
                        dailyAverage = dailyAverage,
                        topArtist = topArtist?.artist,
                        topArtistPlayCount = topArtist?.playCount ?: 0,
                        topSong = topSong?.songTitle,
                        topSongArtist = topSong?.songArtist,
                        topSongPlayCount = topSong?.playCount ?: 0,
                        dayStreaks = dayStreaks,
                        dailyStats = dailyStats,
                        monthlyHistory = _uiState.value.monthlyHistory
                    )
                }.collectLatest { newState ->
                    _uiState.value = newState
                }
                
            } catch (e: Exception) {
                Log.e("SoundCapsuleViewModel", "Error loading analytics", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load analytics: ${e.message}"
                )
            }
        }
    }
    
    private fun loadAllAnalytics() {
        viewModelScope.launch {
            analyticsRepository.getAllMonthlyAnalytics().collectLatest { monthlyHistory ->
                _uiState.value = _uiState.value.copy(monthlyHistory = monthlyHistory)
            }
        }
    }
    
    private fun loadDetailedAnalytics() {
        viewModelScope.launch {
            try {
                _analyticsState.value = _analyticsState.value.copy(isLoading = true)
                
                val currentMonth = analyticsRepository.getCurrentMonthString()
                
                combine(
                    analyticsRepository.getTotalListeningTimeForMonth(currentMonth),
                    analyticsRepository.getTopArtistsForMonth(currentMonth, 10),
                    analyticsRepository.getTopSongsForMonth(currentMonth, 10),
                    analyticsRepository.getDailyStatsForMonth(currentMonth),
                    analyticsRepository.getDayStreaksForMonth(currentMonth)
                ) { totalTime, topArtists, topSongs, dailyStats, dayStreaks ->
                    
                    // Ubah daily stats ke daily listening
                    val dailyListening = dailyStats.map { stat ->
                        DailyListening(
                            userID = tokenManager.getCurrentUserID(),
                            date = stat.date,
                            listeningTimeSeconds = stat.totalTimeSeconds,
                        )
                    }
                    
                    // Count unique songs played this month
                    val totalSongsPlayed = topSongs.size
                    
                    AnalyticsUiState(
                        isLoading = false,
                        totalListeningTimeThisMonth = totalTime, // Already in seconds from repository
                        totalSongsPlayedThisMonth = totalSongsPlayed,
                        topArtists = topArtists,
                        topSongs = topSongs,
                        dailyListening = dailyListening,
                        dayStreaks = dayStreaks
                    )
                }.collectLatest { newState ->
                    _analyticsState.value = newState
                }
                
            } catch (e: Exception) {
                Log.e("SoundCapsuleViewModel", "Error loading detailed analytics", e)
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = "Failed to load analytics: ${e.message}"
                )
            }
        }
    }
    
    fun getAnalyticsForMonth(month: String): Flow<SoundCapsuleUiState> {
        return combine(
            analyticsRepository.getTotalListeningTimeForMonth(month),
            analyticsRepository.getTopArtistsForMonth(month, 10),
            analyticsRepository.getTopSongsForMonth(month, 10),
            analyticsRepository.getDayStreaksForMonth(month),
            analyticsRepository.getDailyStatsForMonth(month)
        ) { totalTime, topArtists, topSongs, dayStreaks, dailyStats ->
            
            val topArtist = topArtists.firstOrNull()
            val topSong = topSongs.firstOrNull()
            val dailyAverage = if (dailyStats.isNotEmpty()) {
                totalTime / dailyStats.size
            } else 0L
            
            SoundCapsuleUiState(
                currentMonth = month,
                totalListeningTime = totalTime,
                dailyAverage = dailyAverage,
                topArtist = topArtist?.artist,
                topArtistPlayCount = topArtist?.playCount ?: 0,
                topSong = topSong?.songTitle,
                topSongArtist = topSong?.songArtist,
                topSongPlayCount = topSong?.playCount ?: 0,
                dayStreaks = dayStreaks,
                dailyStats = dailyStats
            )
        }
    }
    
    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}min"
            minutes > 0 -> "${minutes}min"
            else -> "${totalSeconds}s"
        }
    }
    
    fun formatTimeDetailed(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    fun formatDisplayMonth(month: String): String {
        return try {
            val date = dateFormat.parse(month)
            date?.let { displayDateFormat.format(it) } ?: month
        } catch (e: Exception) {
            month
        }
    }
    
    fun exportAnalyticsToCSV(context: Context, month: String) {
        viewModelScope.launch {
            try {
                isExporting = true
                
                val fileName = "purrytify_analytics_${month}.csv"
                
                // Save ke folder Downloads
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                
                // kalau downloads tidak ada, buat foldernya
                downloadsDir.mkdirs()
                
                // Analytics untuk bulan "month"
                val totalTime = analyticsRepository.getTotalListeningTimeForMonth(month).first()
                val topArtists = analyticsRepository.getTopArtistsForMonth(month, 50).first()
                val topSongs = analyticsRepository.getTopSongsForMonth(month, 50).first()
                val dayStreaks = analyticsRepository.getDayStreaksForMonth(month).first()
                val dailyStats = analyticsRepository.getDailyStatsForMonth(month).first()
                
                FileWriter(file).use { writer ->
                    // Write header
                    writer.append("Purrytify Sound Capsule - ${formatDisplayMonth(month)}\n\n")
                    
                    // Summary stats
                    writer.append("SUMMARY\n")
                    writer.append("Total Listening Time,${formatTime(totalTime)}\n")
                    writer.append("Daily Average,${formatTime(if (dailyStats.isNotEmpty()) totalTime / dailyStats.size else 0L)}\n")
                    writer.append("Total Songs Played,${topSongs.size}\n")
                    writer.append("Total Artists,${topArtists.size}\n\n")
                    
                    // Top Artists
                    writer.append("TOP ARTISTS\n")
                    writer.append("Rank,Artist,Play Count,Total Time\n")
                    topArtists.forEachIndexed { index, artist ->
                        writer.append("${index + 1},\"${artist.artist}\",${artist.playCount},${formatTime(artist.totalListeningTime)}\n")
                    }
                    writer.append("\n")
                    
                    // Top Songs
                    writer.append("TOP SONGS\n")
                    writer.append("Rank,Song,Artist,Play Count,Total Time\n")
                    topSongs.forEachIndexed { index, song ->
                        writer.append("${index + 1},\"${song.songTitle}\",\"${song.songArtist}\",${song.playCount},${formatTime(song.totalListeningTime)}\n")
                    }
                    writer.append("\n")
                    
                    // Day Streaks
                    writer.append("DAY STREAKS (2+ consecutive days)\n")
                    writer.append("Song,Artist,Streak Days,Start Date,End Date\n")
                    dayStreaks.forEach { streak ->
                        writer.append("\"${streak.songTitle}\",\"${streak.songArtist}\",${streak.streakDays},${streak.startDate},${streak.endDate}\n")
                    }
                    writer.append("\n")
                    
                    // Daily Breakdown
                    writer.append("DAILY BREAKDOWN\n")
                    writer.append("Date,Total Time,Songs Played\n")
                    dailyStats.forEach { daily ->
                        writer.append("${daily.date},${formatTime(daily.totalTimeSeconds)},${daily.songsPlayed}\n")
                    }
                }
                
                // jika success
                Toast.makeText(
                    context, 
                    "Analytics exported to Downloads/${fileName}", 
                    Toast.LENGTH_LONG
                ).show()
                
            } catch (e: Exception) {
                Log.e("SoundCapsuleViewModel", "Error exporting analytics", e)
                _uiState.value = _uiState.value.copy(error = "Failed to export: ${e.message}")
            } finally {
                isExporting = false
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshAnalytics() {
        loadCurrentMonthAnalytics()
        loadAllAnalytics()
    }
}

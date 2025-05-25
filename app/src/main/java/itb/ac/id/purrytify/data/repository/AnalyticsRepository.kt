package itb.ac.id.purrytify.data.repository

import android.util.Log
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.local.dao.AnalyticsDao
import itb.ac.id.purrytify.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val tokenManager: TokenManager
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val dailyDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private suspend fun requireUserID(): Int {
        return tokenManager.getCurrentUserID()
    }

    suspend fun trackSongPlay(songId: Int, songTitle: String, songArtist: String, songDurationSeconds: Long) {
        val currentTime = System.currentTimeMillis()
        val today = dailyDateFormat.format(Date(currentTime))
        val month = dateFormat.format(Date(currentTime))
        
        Log.d("AnalyticsRepository", "Tracking song play: $songTitle by $songArtist (${songDurationSeconds}s)")
        
        // Update daily listening
        updateDailyListening(today, songDurationSeconds)
        
        // Update song play count
        updateSongPlayCount(songId, songTitle, songArtist, month, songDurationSeconds)
        
        // Update artist play count
        updateArtistPlayCount(songArtist, month, songDurationSeconds)
        
        // Update monthly analytics
        updateMonthlyAnalytics(month)
    }
    
    private suspend fun updateDailyListening(date: String, durationSeconds: Long) {
        val userID = requireUserID()
        
        val existing = analyticsDao.getDailyListening(userID, date)
        if (existing != null) {
            val updated = existing.copy(
                listeningTimeSeconds = existing.listeningTimeSeconds + durationSeconds,
                songsPlayed = existing.songsPlayed + 1
            )
            analyticsDao.insertDailyListening(updated)
        } else {
            val newDaily = DailyListening(
                userID = userID,
                date = date,
                listeningTimeSeconds = durationSeconds,
                songsPlayed = 1
            )
            analyticsDao.insertDailyListening(newDaily)
        }
    }
    
    private suspend fun updateSongPlayCount(songId: Int, songTitle: String, songArtist: String, month: String, durationSeconds: Long) {
        val userID = requireUserID()
        
        val existing = analyticsDao.getSongPlayCount(userID, songId, month)
        if (existing != null) {
            val updated = existing.copy(
                playCount = existing.playCount + 1,
                totalListeningTime = existing.totalListeningTime + durationSeconds,
                lastPlayed = System.currentTimeMillis()
            )
            analyticsDao.insertSongPlayCount(updated)
        } else {
            val newCount = SongPlayCount(
                userID = userID,
                songId = songId,
                songTitle = songTitle,
                songArtist = songArtist,
                month = month,
                playCount = 1,
                totalListeningTime = durationSeconds,
                songDurationSeconds = durationSeconds, // Assume first play represents song duration
                lastPlayed = System.currentTimeMillis()
            )
            analyticsDao.insertSongPlayCount(newCount)
        }
    }
    
    private suspend fun updateArtistPlayCount(artist: String, month: String, durationSeconds: Long) {
        val userID = requireUserID()
        
        val existing = analyticsDao.getArtistPlayCount(userID, artist, month)
        if (existing != null) {
            val updated = existing.copy(
                playCount = existing.playCount + 1,
                totalListeningTime = existing.totalListeningTime + durationSeconds,
                lastPlayed = System.currentTimeMillis()
            )
            analyticsDao.insertArtistPlayCount(updated)
        } else {
            val newCount = ArtistPlayCount(
                userID = userID,
                artist = artist,
                month = month,
                playCount = 1,
                totalListeningTime = durationSeconds,
                lastPlayed = System.currentTimeMillis()
            )
            analyticsDao.insertArtistPlayCount(newCount)
        }
    }
    
    private suspend fun updateMonthlyAnalytics(month: String) {
        val userID = requireUserID()
        
        // Get aggregated data
        val totalTimeSeconds = analyticsDao.getTotalListeningTimeForMonth(userID, "$month%").first()
        val totalSongs = analyticsDao.getTotalSongsPlayedForMonth(userID, month).first()
        val topArtists = analyticsDao.getTopArtistsForMonth(userID, month, 1).first()
        val topSongs = analyticsDao.getTopSongsForMonth(userID, month, 1).first()
        
        val topArtist = topArtists.firstOrNull()
        val topSong = topSongs.firstOrNull()
        
        val analytics = MonthlyAnalytics(
            userID = userID,
            month = month,
            totalListeningTime = totalTimeSeconds * 1000L, // Convert seconds to milliseconds
            songsPlayed = totalSongs,
            topArtist = topArtist?.artist,
            topArtistPlayCount = topArtist?.playCount ?: 0,
            topSong = topSong?.songTitle,
            topSongArtist = topSong?.songArtist,
            topSongPlayCount = topSong?.playCount ?: 0,
            updatedAt = System.currentTimeMillis()
        )
        
        analyticsDao.insertMonthlyAnalytics(analytics)
    }
    
    // Public methods for accessing analytics
    suspend fun getMonthlyStats(month: String): Flow<MonthlyAnalytics?> {
        val userID = requireUserID()
        return analyticsDao.getMonthlyAnalytics(userID, month)
    }
    
    suspend fun getCurrentMonthStats(): Flow<MonthlyAnalytics?> {
        val userID = requireUserID()
        val currentMonth = dateFormat.format(Date())
        return analyticsDao.getMonthlyAnalytics(userID, currentMonth)
    }
    
    fun getTotalListeningTimeForMonth(month: String): Flow<Long> {
        val userID = runBlocking { tokenManager.getCurrentUserID() }
        val monthPattern = "$month%"
        return analyticsDao.getTotalListeningTimeForMonth(userID, monthPattern)
    }
    
    fun getTopArtistsForMonth(month: String, limit: Int = 10): Flow<List<ArtistPlayCount>> {
        val userID = runBlocking { tokenManager.getCurrentUserID() }
        return analyticsDao.getTopArtistsForMonth(userID, month, limit)
    }
    
    fun getTopSongsForMonth(month: String, limit: Int = 10): Flow<List<SongPlayCount>> {
        val userID = runBlocking { tokenManager.getCurrentUserID() }
        return analyticsDao.getTopSongsForMonth(userID, month, limit)
    }
    
    fun getDayStreaksForMonth(month: String): Flow<List<DayStreak>> {
        val userID = runBlocking { tokenManager.getCurrentUserID() }
        return analyticsDao.getDayStreaksForMonth(userID, month)
    }
    
    fun getDailyStatsForMonth(month: String): Flow<List<DailyStats>> {
        val userID = runBlocking { tokenManager.getCurrentUserID() }
        val monthPattern = "$month%"
        return analyticsDao.getDailyStatsForMonth(userID, monthPattern)
    }
    
    fun getAllMonthlyAnalytics(): Flow<List<MonthlyAnalytics>> {
        val userID = runBlocking { tokenManager.getCurrentUserID() }
        return analyticsDao.getAllMonthlyAnalytics(userID)
    }
    
    fun getCurrentMonthString(): String {
        return dateFormat.format(Date())
    }
    
    suspend fun getPreviousMonthString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        return dateFormat.format(cal.time)
    }
    
    // Cleanup old data (simpan 12 bulan terakhir)
//    suspend fun cleanupOldData() {
//        val cutoffCal = Calendar.getInstance()
//        cutoffCal.add(Calendar.MONTH, -12)
//
//        val cutoffDate = dailyDateFormat.format(cutoffCal.time)
//        val cutoffMonth = dateFormat.format(cutoffCal.time)
//
//        analyticsDao.cleanupOldDailyListening(cutoffDate)
//        analyticsDao.cleanupOldSongPlayCounts(cutoffMonth)
//        analyticsDao.cleanupOldArtistPlayCounts(cutoffMonth)
//        analyticsDao.cleanupOldMonthlyAnalytics(cutoffMonth)
//    }
}

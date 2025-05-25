package itb.ac.id.purrytify.data.local.dao

import androidx.room.*
import itb.ac.id.purrytify.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    
    // Daily Listening operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyListening(dailyListening: DailyListening)
    
    @Query("SELECT * FROM daily_listening WHERE userID = :userID AND date = :date")
    suspend fun getDailyListening(userID: Int, date: String): DailyListening?
    
    @Query("SELECT * FROM daily_listening WHERE userID = :userID AND date LIKE :monthPattern ORDER BY date ASC")
    fun getDailyListeningForMonth(userID: Int, monthPattern: String): Flow<List<DailyListening>>
    
    @Query("SELECT COALESCE(SUM(listeningTimeSeconds), 0) FROM daily_listening WHERE userID = :userID AND date LIKE :monthPattern")
    fun getTotalListeningTimeForMonth(userID: Int, monthPattern: String): Flow<Long>
    
    // Song Play Count operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongPlayCount(songPlayCount: SongPlayCount)
    
    @Query("SELECT * FROM song_play_count WHERE userID = :userID AND songId = :songId AND month = :month")
    suspend fun getSongPlayCount(userID: Int, songId: Int, month: String): SongPlayCount?
    
    @Query("SELECT * FROM song_play_count WHERE userID = :userID AND month = :month ORDER BY playCount DESC LIMIT :limit")
    fun getTopSongsForMonth(userID: Int, month: String, limit: Int = 10): Flow<List<SongPlayCount>>
    
    @Query("SELECT COUNT(*) FROM song_play_count WHERE userID = :userID AND month = :month")
    fun getTotalSongsPlayedForMonth(userID: Int, month: String): Flow<Int>
    
    @Query("SELECT COUNT(DISTINCT songId) FROM song_play_count WHERE userID = :userID AND month = :month")
    fun getUniqueSongsPlayedForMonth(userID: Int, month: String): Flow<Int>
    
    // Artist Play Count operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtistPlayCount(artistPlayCount: ArtistPlayCount)
    
    @Query("SELECT * FROM artist_play_count WHERE userID = :userID AND artist = :artist AND month = :month")
    suspend fun getArtistPlayCount(userID: Int, artist: String, month: String): ArtistPlayCount?
    
    @Query("SELECT * FROM artist_play_count WHERE userID = :userID AND month = :month ORDER BY playCount DESC LIMIT :limit")
    fun getTopArtistsForMonth(userID: Int, month: String, limit: Int = 10): Flow<List<ArtistPlayCount>>
    
    // Monthly Analytics operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyAnalytics(monthlyAnalytics: MonthlyAnalytics)
    
    @Query("SELECT * FROM monthly_analytics WHERE userID = :userID AND month = :month")
    fun getMonthlyAnalytics(userID: Int, month: String): Flow<MonthlyAnalytics?>
    
    @Query("SELECT * FROM monthly_analytics WHERE userID = :userID ORDER BY month DESC")
    fun getAllMonthlyAnalytics(userID: Int): Flow<List<MonthlyAnalytics>>
    
    // Day Streaks calculation - simplified to work without listening_session table
    @Query("""
        SELECT songTitle, songArtist, 
               COUNT(DISTINCT date) as streakDays,
               MIN(date) as startDate,
               MAX(date) as endDate
        FROM (
            SELECT DISTINCT s.songTitle, s.songArtist, d.date
            FROM song_play_count s
            JOIN daily_listening d ON d.userID = s.userID AND substr(d.date, 1, 7) = s.month
            WHERE s.userID = :userID AND s.month = :month AND s.playCount > 0
        )
        GROUP BY songTitle, songArtist
        HAVING streakDays >= 2
        ORDER BY streakDays DESC
    """)
    fun getDayStreaksForMonth(userID: Int, month: String): Flow<List<DayStreak>>
    
    // Daily Stats
    @Query("""
        SELECT date, 
               listeningTimeSeconds as totalTimeSeconds, 
               songsPlayed 
        FROM daily_listening 
        WHERE userID = :userID AND date LIKE :monthPattern 
        ORDER BY date ASC
    """)
    fun getDailyStatsForMonth(userID: Int, monthPattern: String): Flow<List<DailyStats>>
    
    // Cleanup operations (simplified, no longer reference listening_session)
    @Query("DELETE FROM daily_listening WHERE date < :cutoffDate")
    suspend fun cleanupOldDailyListening(cutoffDate: String)
    
    @Query("DELETE FROM song_play_count WHERE month < :cutoffMonth")
    suspend fun cleanupOldSongPlayCounts(cutoffMonth: String)
    
    @Query("DELETE FROM artist_play_count WHERE month < :cutoffMonth")
    suspend fun cleanupOldArtistPlayCounts(cutoffMonth: String)
    
    @Query("DELETE FROM monthly_analytics WHERE month < :cutoffMonth")
    suspend fun cleanupOldMonthlyAnalytics(cutoffMonth: String)
}

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
    
    @Query("""
        SELECT spc.*, s.imagePath 
        FROM song_play_count spc 
        LEFT JOIN song s ON spc.songId = s.songId AND spc.userID = s.userID
        WHERE spc.userID = :userID AND spc.month = :month 
        ORDER BY spc.playCount DESC 
        LIMIT :limit
    """)
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
    
    @Query("""
        SELECT apc.*, 
               COALESCE((SELECT s.imagePath 
                FROM song s 
                LEFT JOIN song_play_count spc2 ON s.songId = spc2.songId AND s.userID = spc2.userID 
                WHERE s.artist = apc.artist AND s.userID = apc.userID AND spc2.month = apc.month
                ORDER BY spc2.playCount DESC 
                LIMIT 1), '') as imagePath
        FROM artist_play_count apc 
        WHERE apc.userID = :userID AND apc.month = :month 
        ORDER BY apc.playCount DESC 
        LIMIT :limit
    """)
    fun getTopArtistsForMonth(userID: Int, month: String, limit: Int = 10): Flow<List<ArtistPlayCount>>
    
    // Daily Song Play operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySongPlay(dailySongPlay: DailySongPlay)
    
    @Query("SELECT * FROM daily_song_plays WHERE userID = :userID AND songId = :songId AND date = :date")
    suspend fun getDailySongPlay(userID: Int, songId: Int, date: String): DailySongPlay?
    
    @Query("""
        SELECT dsp.*, s.imagePath
        FROM daily_song_plays dsp
        LEFT JOIN song s ON dsp.songId = s.songId AND dsp.userID = s.userID
        WHERE dsp.userID = :userID AND dsp.date LIKE :monthPattern 
        ORDER BY dsp.date ASC
    """)
    fun getDailySongPlaysForMonth(userID: Int, monthPattern: String): Flow<List<DailySongPlay>>
    
    // Monthly Analytics operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyAnalytics(monthlyAnalytics: MonthlyAnalytics)
    
    @Query("SELECT * FROM monthly_analytics WHERE userID = :userID AND month = :month")
    fun getMonthlyAnalytics(userID: Int, month: String): Flow<MonthlyAnalytics?>
    
    @Query("SELECT * FROM monthly_analytics WHERE userID = :userID ORDER BY month DESC")
    fun getAllMonthlyAnalytics(userID: Int): Flow<List<MonthlyAnalytics>>
    
    @Query("""
        SELECT dsp.*, s.imagePath
        FROM daily_song_plays dsp
        LEFT JOIN song s ON dsp.songId = s.songId AND dsp.userID = s.userID
        WHERE dsp.userID = :userID AND dsp.date LIKE :monthPattern
        ORDER BY dsp.songId, dsp.date ASC
    """)
    fun getSongPlayDatesForMonth(userID: Int, monthPattern: String): Flow<List<DailySongPlay>>
    
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
    
    @Query("DELETE FROM daily_song_plays WHERE date < :cutoffDate")
    suspend fun cleanupOldDailySongPlays(cutoffDate: String)
    
    @Query("DELETE FROM monthly_analytics WHERE month < :cutoffMonth")
    suspend fun cleanupOldMonthlyAnalytics(cutoffMonth: String)
}

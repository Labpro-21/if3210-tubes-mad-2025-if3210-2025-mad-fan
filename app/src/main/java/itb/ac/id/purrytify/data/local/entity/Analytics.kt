package itb.ac.id.purrytify.data.local.entity

import androidx.room.*

@Entity(
    tableName = "monthly_analytics",
    indices = [Index(value = ["userID", "month"], unique = true)]
)
data class MonthlyAnalytics(
    @PrimaryKey(autoGenerate = true)
    val analyticsId: Int = 0,
    val userID: Int,
    val month: String, // Format: "2025-05"
    val totalListeningTime: Long = 0L, // Total time in milliseconds
    val songsPlayed: Int = 0,
    val topArtist: String? = null,
    val topArtistPlayCount: Int = 0,
    val topSong: String? = null,
    val topSongArtist: String? = null,
    val topSongPlayCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_listening",
    indices = [Index(value = ["userID", "date"], unique = true)]
)
data class DailyListening(
    @PrimaryKey(autoGenerate = true)
    val dailyId: Int = 0,
    val userID: Int,
    val date: String, // Format: "2025-05-25"
    val listeningTimeSeconds: Long = 0L, // Time in seconds
    val songsPlayed: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "song_play_count",
    indices = [
        Index(value = ["userID", "songId", "month"], unique = true),
        Index(value = ["userID", "month"])
    ]
)
data class SongPlayCount(
    @PrimaryKey(autoGenerate = true)
    val playCountId: Int = 0,
    val userID: Int,
    val songId: Int,
    val songTitle: String,
    val songArtist: String,
    val songDurationSeconds: Long, // Song duration in seconds
    val month: String, // Format: "2025-05"
    val playCount: Int = 0,
    val totalListeningTime: Long = 0L, // Total listening time in seconds
    val lastPlayed: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "artist_play_count",
    indices = [
        Index(value = ["userID", "artist", "month"], unique = true),
        Index(value = ["userID", "month"])
    ]
)
data class ArtistPlayCount(
    @PrimaryKey(autoGenerate = true)
    val artistPlayCountId: Int = 0,
    val userID: Int,
    val artist: String,
    val month: String, // Format: "2025-05"
    val playCount: Int = 0,
    val totalListeningTime: Long = 0L, // Total listening time in seconds
    val lastPlayed: Long = System.currentTimeMillis()
)

// Data classes for aggregated analytics
data class MonthlyStats(
    val month: String,
    val totalListeningTimeSeconds: Long,
    val dailyAverageSeconds: Long,
    val songsPlayed: Int,
    val topArtist: String?,
    val topArtistPlayCount: Int,
    val topSong: String?,
    val topSongArtist: String?,
    val topSongPlayCount: Int
)

data class DayStreak(
    val songTitle: String,
    val songArtist: String,
    val streakDays: Int,
    val startDate: String,
    val endDate: String
)

data class DailyStats(
    val date: String,
    val totalTimeSeconds: Long,
    val songsPlayed: Int
)

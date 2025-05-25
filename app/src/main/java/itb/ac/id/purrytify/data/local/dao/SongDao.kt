package itb.ac.id.purrytify.data.local.dao

import androidx.room.*
import itb.ac.id.purrytify.data.local.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    // Init Song DAO with simple queries

    @Query("SELECT * FROM song WHERE userID = :userId")
    fun getAll(userId: Int): Flow<List<Song>>

    @Query("SELECT * FROM song WHERE userID = :userId AND isLiked = 1")
    fun getLiked(userId: Int): Flow<List<Song>>

    @Query("SELECT * FROM song WHERE userID = :userId AND isDownloaded = 1")
    fun getDownloaded(userId: Int): Flow<List<Song>>

    @Query("SELECT * FROM song WHERE userID = :userId ORDER BY createdAt DESC LIMIT 10")
    fun getNew(userId: Int): Flow<List<Song>>

    @Query("SELECT * FROM song WHERE userID = :userId AND lastPlayed IS NOT NULL ORDER BY lastPlayed DESC LIMIT 10")
    fun getRecentlyPlayed(userId: Int): Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM song WHERE userID = :userId")
    fun getAllCount(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM song WHERE userID = :userId AND isLiked = 1")
    fun getLikedCount(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM song WHERE userID = :userId AND lastPlayed IS NOT NULL")
    fun getPlayedCount(userId: Int): Flow<Int>

    @Query("SELECT * FROM song WHERE userID = :userId AND songId = :songId")
    suspend fun getById(songId: Int, userId: Int): Song?

    @Query("SELECT * FROM song WHERE userID = :userId AND songId > :songId LIMIT 1")
    suspend fun getNextSong(songId: Int, userId: Int): Song?

    @Query("SELECT * FROM song WHERE userID = :userId AND songId < :songId ORDER BY songId DESC LIMIT 1")
    suspend fun getPreviousSong(songId: Int, userId: Int): Song?

    @Query("SELECT * FROM song WHERE userID = :userId AND isLiked = 1 AND lastPlayed IS NOT NULL ORDER BY lastPlayed DESC LIMIT 10")
    suspend fun getRecommended(userId: Int): List<Song>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg song: Song): List<Long> // return id

    @Delete
    suspend fun delete(vararg song: Song)

    @Update
    suspend fun update(vararg song: Song)
}


package itb.ac.id.purrytify.data.local.dao

import androidx.room.*
import itb.ac.id.purrytify.data.local.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    // Init Song DAO with simple queries

    @Query("SELECT * FROM song WHERE userID = :userId")
    fun getAll(userId: Int): Flow<List<Song>>

    @Query("SELECT * FROM song WHERE userID = :userId AND songId = :songId")
    suspend fun getById(songId: Int, userId: Int): Song?

    @Query("SELECT * FROM song WHERE userID = :userId AND songId > :songId LIMIT 1")
    suspend fun getNextSong(songId: Int, userId: Int): Song?

    @Query("SELECT * FROM song WHERE userID = :userId AND songId < :songId ORDER BY songId DESC LIMIT 1")
    suspend fun getPreviousSong(songId: Int, userId: Int): Song?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg song: Song): List<Long> // return id

    @Delete
    suspend fun delete(vararg song: Song)

    @Update
    suspend fun update(vararg song: Song)
}


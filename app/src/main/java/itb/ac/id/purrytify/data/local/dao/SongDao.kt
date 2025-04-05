package itb.ac.id.purrytify.data.local.dao

import androidx.room.*
import itb.ac.id.purrytify.data.local.entity.Song

@Dao
interface SongDao {
    // Init Song DAO with simple queries

    @Query("SELECT * FROM song")
    suspend fun getAll(): List<Song>

    @Query("SELECT * FROM song WHERE songId = :songId")
    suspend fun getById(songId: Int): Song?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg song: Song): List<Long> // return id

    @Delete
    suspend fun delete(vararg song: Song)

    @Update
    suspend fun update(vararg song: Song)
}


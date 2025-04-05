package itb.ac.id.purrytify.data.local.dao

import androidx.room.*
import itb.ac.id.purrytify.data.local.entity.LikedSong
import itb.ac.id.purrytify.data.local.entity.Song

@Dao
interface LikedSongDao {
    @Query("SELECT * FROM liked_song")
    suspend fun getAll(): List<LikedSong>

    @Query("SELECT * FROM liked_song WHERE songId = :songId")
    suspend fun getById(songId: Int): LikedSong?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vararg song: LikedSong): List<Long> // return id

    @Delete
    suspend fun delete(vararg song: LikedSong)

}
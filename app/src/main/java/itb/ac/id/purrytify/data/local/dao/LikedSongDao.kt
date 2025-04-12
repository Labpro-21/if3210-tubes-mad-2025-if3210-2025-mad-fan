//package itb.ac.id.purrytify.data.local.dao
//
//import androidx.room.*
//import itb.ac.id.purrytify.data.local.entity.LikedSong
//import itb.ac.id.purrytify.data.local.entity.Song
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface LikedSongDao {
//    @Query("SELECT * FROM liked_song NATURAL JOIN song")
//    fun getAll(): Flow<List<Song>>
//
//    @Query("SELECT * FROM liked_song NATURAL JOIN song WHERE songId = :songId")
//    suspend fun getById(songId: Int): Song?
//
//    @Insert(onConflict = OnConflictStrategy.ABORT)
//    suspend fun insert(vararg song: LikedSong): List<Long> // return id
//
//    @Delete
//    suspend fun delete(vararg song: LikedSong)
//
//}
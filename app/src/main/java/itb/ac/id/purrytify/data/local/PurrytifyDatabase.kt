package itb.ac.id.purrytify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
//import itb.ac.id.purrytify.data.local.dao.LikedSongDao
import itb.ac.id.purrytify.data.local.dao.SongDao
//import itb.ac.id.purrytify.data.local.entity.LikedSong
import itb.ac.id.purrytify.data.local.entity.Song

@Database(
    entities = [Song::class],
    version = 3,
    exportSchema = false
)
abstract class PurrytifyDatabase: RoomDatabase() {
    abstract fun songDao(): SongDao
//    abstract fun likedSongDao(): LikedSongDao


}
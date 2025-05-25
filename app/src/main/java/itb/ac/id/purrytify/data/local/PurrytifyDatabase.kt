package itb.ac.id.purrytify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import itb.ac.id.purrytify.data.local.dao.AnalyticsDao
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.*

@Database(
    entities = [
        Song::class,
        MonthlyAnalytics::class,
        DailyListening::class,
        SongPlayCount::class,
        ArtistPlayCount::class
    ],
    version = 8,
    exportSchema = false
)
abstract class PurrytifyDatabase: RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun analyticsDao(): AnalyticsDao
}
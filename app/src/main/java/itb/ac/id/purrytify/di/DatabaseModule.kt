package itb.ac.id.purrytify.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import itb.ac.id.purrytify.data.local.PurrytifyDatabase
import itb.ac.id.purrytify.data.local.dao.SongDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): PurrytifyDatabase {
        return Room.databaseBuilder(
            app,
            PurrytifyDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: PurrytifyDatabase): SongDao {
        return database.songDao()
    }

//    @Provides
//    @Singleton
//    fun provideLikedSongDao(database: PurrytifyDatabase): LikedSongDao {
//        return database.likedSongDao()
//    }
}
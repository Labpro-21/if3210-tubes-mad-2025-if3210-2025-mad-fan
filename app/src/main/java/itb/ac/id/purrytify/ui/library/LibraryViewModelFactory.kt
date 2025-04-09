package itb.ac.id.purrytify.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import itb.ac.id.purrytify.data.local.dao.LikedSongDao
import itb.ac.id.purrytify.data.local.dao.SongDao

class LibraryViewModelFactory(
    private val songDao: SongDao,
    private val likedSongDao: LikedSongDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            return LibraryViewModel(songDao, likedSongDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
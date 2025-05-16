package itb.ac.id.purrytify.ui.library

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.ui.player.SongPlayerViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songDao: SongDao,
    private val tokenManager: TokenManager,
) : ViewModel() {
    private val _allSongs = MutableLiveData<List<Song>>()
    val allSongs: LiveData<List<Song>> = _allSongs
    private val _likedSongs = MutableLiveData<List<Song>>()
    val likedSongs: LiveData<List<Song>> = _likedSongs
    private val _downloadedSongs = MutableLiveData<List<Song>>()
    val downloadedSongs: LiveData<List<Song>> = _downloadedSongs

    init {
        loadAllSongs()
        loadLikedSongs()
        loadDownloadedSongs()
    }

    private fun loadAllSongs() {
        viewModelScope.launch {
            val userID = tokenManager.getCurrentUserID()
            songDao.getAll(userID).collect { songs ->
                _allSongs.postValue(songs)
            }
        }
    }

    private fun loadLikedSongs() {
        viewModelScope.launch {
            val userID = tokenManager.getCurrentUserID()
            songDao.getLiked(userID).collect { songs ->
                _likedSongs.postValue(songs)
            }
        }
    }

    private fun loadDownloadedSongs() {
        viewModelScope.launch {
            val userID = tokenManager.getCurrentUserID()
            songDao.getDownloaded(userID).collect { songs ->
                _downloadedSongs.postValue(songs)
            }
        }
    }

}
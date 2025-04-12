package itb.ac.id.purrytify.ui.home
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songDao: SongDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _newSongs = MutableStateFlow<List<Song>>(emptyList())
    val newSongs: StateFlow<List<Song>> = _newSongs

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed

    init {
        viewModelScope.launch {
            val userId = tokenManager.getCurrentUserID()

            songDao.getNew(userId).collect {
                _newSongs.value = it
            }
        }
        viewModelScope.launch {
            val userId = tokenManager.getCurrentUserID()

            songDao.getRecentlyPlayed(userId).collect {
                _recentlyPlayed.value = it
            }
        }
    }
}

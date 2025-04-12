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

    init {
        loadAllSongs()
    }

    private fun loadAllSongs() {
        viewModelScope.launch {
            val userID = tokenManager.getCurrentUserID()
            songDao.getAll(userID).collect { songs ->
                _allSongs.postValue(songs)
            }
        }
    }
//    val likedSongs: LiveData<List<Song>> = likedSongDao.getAll().asLiveData()

//        Dummy Data nanti dihapus
//        _allSongs.value = listOf(
//            Song(1, "Starboy", "The Weeknd, Daft Punk", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_starboy}", 100),
//            Song(2, "Here Comes The Sun", "The Beatles", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_abbey_road}", 100),
//            Song(3, "Midnight Pretenders", "Tomoko Aran", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_midnight_pretenders}", 100),
//            Song(4, "Violent Crimes", "Kanye West", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_ye}", 100),
//            Song(5, "Jazz is for ordinary people", "berlioz", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_jazz}", 100),
//            Song(6, "Loose", "Daniel Caesar", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_loose}", 100),
//            Song(7, "Nights", "Frank Ocean", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_blonde}", 100),
//            Song(8, "Kiss of Life", "Sade", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_sade}", 100),
//            Song(9, "BEST INTEREST", "Tyler, The Creator", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_best_interest}", 100)
//        )

}
package itb.ac.id.purrytify.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.dao.LikedSongDao
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val songDao: SongDao,
    private val likedSongDao: LikedSongDao
) : ViewModel() {

    private val _allSongs = MutableLiveData<List<Song>>(emptyList())
    val allSongs: LiveData<List<Song>> = _allSongs

    private val _likedSongs = MutableLiveData<List<Song>>(emptyList())
    val likedSongs: LiveData<List<Song>> = _likedSongs

    init {
        loadAllSongs()
        loadLikedSongs()
    }

    private fun loadAllSongs() {
//        viewModelScope.launch {
//            _allSongs.value = songDao.getAll()
//        }

//        Dummy Data nanti dihapus
        _allSongs.value = listOf(
            Song(1, "Starboy", "The Weeknd, Daft Punk", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_starboy}", 100),
            Song(2, "Here Comes The Sun", "The Beatles", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_abbey_road}", 100),
            Song(3, "Midnight Pretenders", "Tomoko Aran", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_midnight_pretenders}", 100),
            Song(4, "Violent Crimes", "Kanye West", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_ye}", 100),
            Song(5, "Jazz is for ordinary people", "berlioz", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_jazz}", 100),
            Song(6, "Loose", "Daniel Caesar", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_loose}", 100),
            Song(7, "Nights", "Frank Ocean", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_blonde}", 100),
            Song(8, "Kiss of Life", "Sade", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_sade}", 100),
            Song(9, "BEST INTEREST", "Tyler, The Creator", "", "android.resource://itb.ac.id.purrytify/${R.drawable.cover_best_interest}", 100)
        )

    }

    private fun loadLikedSongs() {
        viewModelScope.launch {
            val likedSongIds = likedSongDao.getAll().map { it.songId }
            val songs = songDao.getAll().filter { song -> likedSongIds.contains(song.songId) }
            _likedSongs.value = songs
        }
    }

    fun playSong(songId: Int) {
        // function buat play song
    }
}
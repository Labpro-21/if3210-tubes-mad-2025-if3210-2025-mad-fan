package itb.ac.id.purrytify.ui.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem.fromUri
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.ui.navigation.NavigationItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SongPlayerViewModel @Inject constructor(
    private val application: Application,
    private val songDao: SongDao,
    private val tokenManager: TokenManager
) : AndroidViewModel(application){

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position

    private val _hasSongEnded = MutableStateFlow(false)
    val hasSongEnded: StateFlow<Boolean> = _hasSongEnded

//    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
//    val playlist: StateFlow<List<Song>> = _playlist
//
//    private var currentIndex = -1
//
//    fun setPlaylist(playlist: List<Song>) {
//        _playlist.value = playlist
//    }

    // Untuk store route last screen
    private var lastScreenRoute: String = NavigationItem.Home.route

    fun setLastScreenRoute(route: String) {
        lastScreenRoute = route
    }

    fun getLastScreenRoute(): String {
        return lastScreenRoute
    }

    val songPlayer: ExoPlayer = ExoPlayer.Builder(getApplication()).build()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    checkUpdateDuration()
                    Log.d("SongPlayer", "Playing song: ${_currentSong.value?.title}")
                }
                Player.STATE_ENDED -> {
                    _currentSong.value = null
                    _hasSongEnded.value = true
                    Log.d("SongPlayer", "Playing song: ${_currentSong.value?.title}")
                }

                Player.STATE_BUFFERING -> {

                }

                Player.STATE_IDLE -> {

                }
            }
        }
    }

    init {
        songPlayer.addListener(playerListener)
        startUpdatingPosition()
    }

    fun playSong(song: Song) {
        val updatedSong = song.copy(lastPlayed = System.currentTimeMillis())
        _currentSong.value = updatedSong
        Log.d("SongPlayer", "Playing song: ${updatedSong.title}")

        songPlayer.setMediaItem(fromUri(updatedSong.filePath))
        songPlayer.prepare()
        songPlayer.play()

        viewModelScope.launch {
            songDao.update(updatedSong)
        }
    }
    fun togglePlayPause() {
        if (songPlayer.isPlaying) {
            songPlayer.pause()
            _isPlaying.value = false
        } else {
            songPlayer.play()
            _isPlaying.value = true
        }
    }

    fun stopSong() {
        songPlayer.stop()
    }

    fun resetHasSongEnded() {
        _hasSongEnded.value = false
    }

    fun nextSong() {
        viewModelScope.launch {
            val currentID = _currentSong.value?.songId
            val nextSong = currentID?.let { songDao.getNextSong(it, tokenManager.getCurrentUserID()) }
                ?: return@launch
            playSong(nextSong)
        }
    }

    fun previousSong() {
        viewModelScope.launch {
            val currentID = _currentSong.value?.songId
            val previousSong = currentID?.let { songDao.getPreviousSong(it, tokenManager.getCurrentUserID()) }
                ?: return@launch
            playSong(previousSong)
        }
    }

    fun seekTo(position: Long) {
        songPlayer.seekTo(position)
    }
    private fun startUpdatingPosition() {
        viewModelScope.launch {
            while (true) {
                _position.value = songPlayer.currentPosition
                delay(200)
            }
        }
    }

    private fun checkUpdateDuration(){
        val song = _currentSong.value ?: return
        if (song.duration == 0L && songPlayer.duration > 0) {
            viewModelScope.launch {
                val updatedSong = song.copy(duration = songPlayer.duration)
                songDao.update(updatedSong)
                _currentSong.value = updatedSong
            }
        }
    }

    fun toggleFavorite() {
        val currentSong = _currentSong.value ?: return
        viewModelScope.launch {
            val updatedSong = if (currentSong.isLiked) {
                currentSong.copy(isLiked = false)
            } else {
                currentSong.copy(isLiked = true)
            }

            // Update the song in the database
            songDao.update(updatedSong)

            // Update the local state
            _currentSong.value = updatedSong
            Log.d("SongPlayer", "Toggled favorite for song: ${updatedSong.title}, isLiked: ${updatedSong.isLiked}")
        }
    }

    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        songPlayer.removeListener(playerListener)
        songPlayer.release()
    }
}
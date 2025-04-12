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

    private val _songQueue = MutableStateFlow<List<Song>>(emptyList())
    val songQueue: StateFlow<List<Song>> = _songQueue

    private var currentIndex = -1

    private val _isQueueEmpty = MutableStateFlow(false)
    val isQueueEmpty: StateFlow<Boolean> = _isQueueEmpty

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled

    enum class RepeatMode {
        OFF, ONE, ALL
    }

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

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
                    if (_repeatMode.value == RepeatMode.ONE) {
                        playAtIndex(currentIndex)
                        Log.d("SongPlayer", "Repeating song: ${_currentSong.value?.title}")
                    } else if (_repeatMode.value == RepeatMode.ALL) {
                        nextSong()
                    } else {
                        if (_songQueue.value.isEmpty()) {
                            _isQueueEmpty.value = true
                            songPlayer.stop()
                            _currentSong.value = null
                            Log.d("SongPlayer", "Queue finished, no more songs to play.")
                        } else {
                            nextSong()
                        }
                    }
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
        _songQueue.value = listOf(song)
        _isQueueEmpty.value = _songQueue.value.isEmpty()
        currentIndex = 0
        playAtIndex(currentIndex)
    }

    private fun playAtIndex(index: Int) {
        if (index < 0 || index >= _songQueue.value.size) return
        val originalSong = _songQueue.value[index]
        val updatedSong = originalSong.copy(lastPlayed = System.currentTimeMillis())


        val newQueue = _songQueue.value.toMutableList()
        newQueue[index] = updatedSong
        _songQueue.value = newQueue

        currentIndex = index
        _currentSong.value = updatedSong

        songPlayer.setMediaItem(fromUri(updatedSong.filePath))
        songPlayer.prepare()
        songPlayer.play()

        viewModelScope.launch {
            songDao.update(updatedSong)
        }
    }

    fun addQueue(song: Song) {
        val updatedQueue = _songQueue.value.toMutableList()
        updatedQueue.add(song)
        _songQueue.value = updatedQueue
        _isQueueEmpty.value = _songQueue.value.isEmpty()
        Log.d("SongPlayer", "Added song to queue: ${song.title}")
        if (currentIndex == -1) {
            currentIndex = 0
            playAtIndex(currentIndex)
        }
        Log.d("SongPlayer", "Current queue: ${_songQueue.value.map { it.title }}")
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
        _currentSong.value = null
        _songQueue.value = emptyList()
        _isQueueEmpty.value = true
    }

    fun nextSong() {
        val queue = _songQueue.value
        if (queue.isEmpty() || currentIndex >= queue.lastIndex) {
            if (_repeatMode.value == RepeatMode.ALL) {
                playAtIndex(0)
            }
            return
        }
        Log.d("SongPlayer", "Playing current song: ${queue[currentIndex].title}")
        playAtIndex(currentIndex + 1)
        Log.d("SongPlayer", "Playing next song: ${queue[currentIndex].title}")
    }

    fun previousSong() {
        val queue = _songQueue.value
        if (queue.isEmpty() || currentIndex <= 0) return
        playAtIndex(currentIndex - 1)
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
    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        if (_isShuffleEnabled.value) {
            Log.d("SongPlayer", "Shuffling song queue")
            _songQueue.value = _songQueue.value.shuffled()
            Log.d("SongPlayer", "Shuffled song queue: ${_songQueue.value.map { it.title }}")
            Log.d("SongPlayer", "Current index: $currentIndex")
            currentIndex = _songQueue.value.indexOf(_currentSong.value)
            Log.d("SongPlayer", "New current index: $currentIndex")
        } else {
            _songQueue.value = _songQueue.value.sortedBy { it.lastPlayed }
            Log.d("SongPlayer", "Unshuffled song queue: ${_songQueue.value.map { it.title }}")
            currentIndex = _songQueue.value.indexOf(_currentSong.value)
        }
    }

    fun toggleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.OFF
        }
        Log.d("SongPlayer", "Repeat mode changed to: ${_repeatMode.value}")
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
        _songQueue.value = emptyList()
        _currentSong.value = null
    }

    fun deleteSong() {
        viewModelScope.launch {
            songDao.delete(_currentSong.value!!)
        }
        nextSong()
    }
}
package itb.ac.id.purrytify.ui.player

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem.fromUri
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.data.model.toSong
import itb.ac.id.purrytify.service.NotificationService
import itb.ac.id.purrytify.ui.navigation.NavigationItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import itb.ac.id.purrytify.data.repository.OnlineSongRepository

@HiltViewModel
class SongPlayerViewModel @Inject constructor(
    private val application: Application,
    private val songDao: SongDao,
    private val onlineSongRepository: OnlineSongRepository
) : AndroidViewModel(application){
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering

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

    // Notification service
    private var notificationService: NotificationService? = null
    private var serviceBound = false

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as NotificationService.NotificationBinder
            notificationService = binder.getService()
            notificationService?.setPlayer(songPlayer)
            serviceBound = true
            currentSong.value?.let {
                notificationService?.updateCurrentSong(it)
            }

            notificationService?.setPlayerCallback(object : NotificationService.PlayerCallback {
                override fun onPlayPause() {
                    togglePlayPause()
                }

                override fun onNext() {
                    Log.d("SongPlayerViewModel", "onNext callback triggered")
                    nextSong()
                }

                override fun onPrevious() {
                    Log.d("SongPlayerViewModel", "onPrevious callback triggered")
                    previousSong()
                }

                override fun onStop() {
                    stopSong()
                }
                override fun onToggleFavorite() {
                    Log.d("SongPlayerViewModel", "onToggleFavorite callback triggered")
                    toggleFavorite()
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            notificationService = null
            serviceBound = false
        }
    }

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
            viewModelScope.launch {
                notificationService?.updateNotification()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    checkUpdateDuration()
                    _isBuffering.value = false
                    notificationService?.updateNotification()
                }
                Player.STATE_ENDED -> {
                    _isBuffering.value = false
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
                            stopNotificationService()
                            Log.d("SongPlayer", "Queue finished, no more songs to play.")
                        } else {
                            nextSong()
                        }
                    }
                }
                Player.STATE_BUFFERING -> {
                    _isBuffering.value = true
                }
                Player.STATE_IDLE -> {
                    _isBuffering.value = false
                }
            }
        }
    }

    init {
        songPlayer.addListener(playerListener)
        startUpdatingPosition()
        bindNotificationService()
    }

    private fun bindNotificationService() {
        val intent = Intent(application, NotificationService::class.java)
        application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun startNotificationService() {
        if (!serviceBound) {
            bindNotificationService()
        }

        val intent = Intent(application, NotificationService::class.java)
        application.startForegroundService(intent)

        currentSong.value?.let {
            notificationService?.updateCurrentSong(it)
        }
    }

    private fun stopNotificationService() {
        if (serviceBound) {
            try {
                application.unbindService(serviceConnection)
                serviceBound = false
            } catch (e: Exception) {
                Log.e("SongPlayerViewModel", "Error unbinding service: ${e.message}")
            }
        }

        val intent = Intent(application, NotificationService::class.java)
        application.stopService(intent)
    }

    fun playSong(song: Song) {
//        val song = OnlineSongResponse(
//            id = 71,
//            title = "Die With A Smile",
//            artist = "Lady Gaga, Bruno Mars",
//            artwork = "https://storage.googleapis.com/mad-public-bucket/cover/Die%20With%20A%20Smile.png",
//            url = "https://storage.googleapis.com/mad-public-bucket/mp3/Lady%20Gaga%2C%20Bruno%20Mars%20-%20Die%20With%20A%20Smile%20(Lyrics).mp3",
//            duration = "4:12",
//            country = "GLOBAL",
//            rank = 1,
//            createdAt = "2025-05-08T02:16:53.192Z",
//            updatedAt = "2025-05-08T02:16:53.192Z"
//        ).toSong(userId = 1)
        _songQueue.value = listOf(song)
        _isQueueEmpty.value = _songQueue.value.isEmpty()
        currentIndex = 0
        playAtIndex(currentIndex)
        startNotificationService()
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

        // Update notification
        notificationService?.updateCurrentSong(updatedSong)

        viewModelScope.launch {
            if (!updatedSong.isOnline) {
                songDao.update(updatedSong)
            }
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

        stopNotificationService()
    }

    fun nextSong() {
        val queue = _songQueue.value
        if (queue.isEmpty() || currentIndex >= queue.lastIndex) {
            if (_repeatMode.value == RepeatMode.ALL) {
                playAtIndex(0)
            }
            stopSong()
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
        Log.d("SongPlayer", "Updating song duration: ${song.title}, original: ${song.duration}, duration: ${songPlayer.duration}")
        if (song.duration != songPlayer.duration) {
            viewModelScope.launch {
                val updatedSong = song.copy(duration = songPlayer.duration)
                if (!updatedSong.isOnline) {
                    songDao.update(updatedSong)
                }
                _currentSong.value = updatedSong
                Log.d("SongPlayer", "Updating song duration: ${_currentSong.value!!.title}, original: ${_currentSong.value!!.duration}, duration: ${songPlayer.duration}")

            }
        }
    }

    fun toggleFavorite() {
        val currentSong = _currentSong.value ?: return
        viewModelScope.launch {

            val updatedSong = currentSong.copy(isLiked = !currentSong.isLiked)
            // Update the song in the database
            if (!updatedSong.isOnline) {
                songDao.update(updatedSong)
            }
            // Update the local state
            _currentSong.value = updatedSong
            // Update the song in the queue
            val index = _songQueue.value.indexOf(currentSong)
            if (index != -1) {
                val newQueue = _songQueue.value.toMutableList()
                newQueue[index] = updatedSong
                _songQueue.value = newQueue
            }
            // Update notification to show the new favorite status
            notificationService?.updateCurrentSong(updatedSong)
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

        // Unbind dan stop notification service
        stopNotificationService()
    }

    fun deleteSong() {
        viewModelScope.launch {
            _currentSong.value?.let {
                if (!it.isOnline) {
                    songDao.delete(it)
                }
            }
        }
        stopSong()
        _isPlaying.value = false
        _position.value = 0L

        stopNotificationService()
    }

    fun playOnlineSong(id: String) {
        viewModelScope.launch {
            try {
                val song = onlineSongRepository.getOnlineSongById(id)?.toSong()
                if (song == null) {
                    Log.e("SongPlayerViewModel", "Song not found")
                    return@launch
                }
                playSong(song)
            } catch (e: Exception) {
                Log.e("SongPlayerViewModel", "Error fetching online song: ${e.message}")
            }
        }
    }
}
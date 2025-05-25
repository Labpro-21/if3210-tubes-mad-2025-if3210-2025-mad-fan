package itb.ac.id.purrytify.ui.player

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
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
import java.util.Locale
import javax.inject.Inject
import itb.ac.id.purrytify.data.repository.AnalyticsRepository
import itb.ac.id.purrytify.data.repository.OnlineSongRepository
import itb.ac.id.purrytify.utils.*
import itb.ac.id.purrytify.utils.AudioDevice
import itb.ac.id.purrytify.utils.AudioDeviceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@HiltViewModel
class SongPlayerViewModel @Inject constructor(
    private val application: Application,
    private val songDao: SongDao,
    private val onlineSongRepository: OnlineSongRepository,
    private val analyticsRepository: AnalyticsRepository
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

    private lateinit var connectivityObserver: ConnectivityObserver
    val _networkStatus = MutableStateFlow(ConnectionStatus.Available)
    val networkStatus: StateFlow<ConnectionStatus> = _networkStatus

    private var lastPosition: Long = 0L
    private var positionSeekWhileOffline: Long? = null

    // Audio device management
    private lateinit var audioDeviceManager: AudioDeviceManager
    private val _currentAudioDevice = MutableStateFlow<AudioDevice?>(null)
    val currentAudioDevice: StateFlow<AudioDevice?> = _currentAudioDevice

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
                override fun onDismissStop() {
                    stopSong()
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
                    Log.d("SongPlayer", "Buffering...")

                    if (_currentSong.value?.isOnline == true &&
                        _networkStatus.value != ConnectionStatus.Available
                    ) {
                        songPlayer.pause()
                        _isPlaying.value = false
                        Log.d("SongPlayer", "Paused due to no internet while buffering")
                    }
                }
                Player.STATE_IDLE -> {
                    _isBuffering.value = false
                }
            }
        }
    }

    init {
        observeNetwork()
        songPlayer.addListener(playerListener)
        startUpdatingPosition()
        bindNotificationService()
        
        // inisialisasi audio device 
        audioDeviceManager = AudioDeviceManager(application)
    }

    @OptIn(FlowPreview::class)
    private fun observeNetwork() {
        connectivityObserver = NetworkConnectivityObserver(application)
        viewModelScope.launch {
            connectivityObserver.observe().debounce(500).collect { status ->
                Log.d("SongPlayerViewModel", "Network status changed: $status")
                handleNetworkChanges(status)
            }
        }
    }

    private fun handleNetworkChanges(status: ConnectionStatus) {
        _networkStatus.value = status
        when (status) {
            ConnectionStatus.Available -> {
                Log.d("SongPlayerViewModel", "Network reconnected")
                if (_currentSong.value?.isOnline == true) {
                    val resumePosition = positionSeekWhileOffline ?: lastPosition
                    if (songPlayer.currentPosition != resumePosition) {
                        songPlayer.seekTo(resumePosition)
                    }
                    positionSeekWhileOffline = null
                    if (!songPlayer.isPlaying) {
                        songPlayer.play()
                        _isPlaying.value = true
                    }
                }
            }
            ConnectionStatus.Lost, ConnectionStatus.Unavailable -> {
                Log.d("SongPlayerViewModel", "Network disconnected")
                if (_currentSong.value?.isOnline == true) {
                    lastPosition = songPlayer.currentPosition
                    positionSeekWhileOffline = null
                    songPlayer.pause()
                    _isPlaying.value = false
                }
            }
            else -> {}
        }
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
        _songQueue.value = listOf(song)
        _isQueueEmpty.value = _songQueue.value.isEmpty()
        currentIndex = 0
        
        ensureAudioOutput()
        
        playAtIndex(currentIndex)
        startNotificationService()
    }

    private fun ensureAudioOutput() {
        try {
            val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            Log.e("SongPlayerViewModel", "Error ensuring audio output: ${e.message}")
        }
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
                // Only track song play for local songs
                val durationSeconds = updatedSong.duration / 1000
                analyticsRepository.trackSongPlay(
                    songId = updatedSong.songId,
                    songTitle = updatedSong.title,
                    songArtist = updatedSong.artist,
                    songDurationSeconds = durationSeconds
                )
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
        if (_networkStatus.value == ConnectionStatus.Available || _currentSong.value?.isOnline == false) {
            if (songPlayer.isPlaying) {
                songPlayer.pause()
                _isPlaying.value = false
            } else {
                songPlayer.play()
                _isPlaying.value = true
            }
            return
        } else {
            Log.d("SongPlayer", "Cannot toggle play/pause while offline for online song")
            return
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
            }else{
                stopSong()
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
        if (_networkStatus.value == ConnectionStatus.Available || _currentSong.value?.isOnline == false) {
            songPlayer.seekTo(position)
            _position.value = position
            positionSeekWhileOffline = null
        } else {
            Log.d("SongPlayer", "Queued seek to $position while offline")
            positionSeekWhileOffline = position
            _position.value = position
        }
    }

    private fun startUpdatingPosition() {
        viewModelScope.launch {
            while (true) {
                if (positionSeekWhileOffline == null || _currentSong.value?.isOnline == false) {
                    _position.value = songPlayer.currentPosition
                }
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
        // reset audio routing
        resetAudioRouting()
        // Unbind dan stop notification service
        stopNotificationService()
    }

    private fun resetAudioRouting() {
        try {
            if (::audioDeviceManager.isInitialized) {
                val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.mode = AudioManager.MODE_NORMAL
                audioManager.isSpeakerphoneOn = false
                if (audioManager.isBluetoothScoOn) {
                    audioManager.stopBluetoothSco()
                    audioManager.isBluetoothScoOn = false
                }
            }
        } catch (e: Exception) {
            Log.e("SongPlayerViewModel", "Error resetting audio routing: ${e.message}")
        }
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

    // Audio device management
    fun getAvailableAudioDevices(): List<AudioDevice> {
        return audioDeviceManager.getAvailableAudioDevices()
    }

    fun setAudioDevice(device: AudioDevice) {
        val success = audioDeviceManager.setAudioDevice(device)
        if (success) {
            viewModelScope.launch {
                delay(300)
                getCurrentAudioDevice()
            }
            Log.d("SongPlayerViewModel", "Audio device set to: ${device.name}")
        } else {
            Log.e("SongPlayerViewModel", "Failed to set audio device: ${device.name}")
        }
    }

    fun getCurrentAudioDevice() {
        val device = audioDeviceManager.getCurrentAudioDevice()
        _currentAudioDevice.value = device
        Log.d("SongPlayerViewModel", "Current audio device: ${device?.name ?: "Unknown"}")
    }
}
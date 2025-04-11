package itb.ac.id.purrytify.ui.player

import android.app.Application
import android.media.MediaPlayer
import android.media.browse.MediaBrowser.MediaItem
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem.fromUri
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.local.entity.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongPlayerViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application){

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position

    val songPlayer: ExoPlayer = ExoPlayer.Builder(getApplication()).build()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                _currentSong.value = null
            }
        }
    }

    init {
        songPlayer.addListener(playerListener)
        startUpdatingPosition()
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        songPlayer.setMediaItem(fromUri(song.filePath))
        songPlayer.prepare()
        songPlayer.play()

    }
    fun togglePlayPause() {
        if (songPlayer.isPlaying) {
            songPlayer.pause()
        } else {
            songPlayer.play()
        }
    }

    fun stopSong() {
        songPlayer.stop()
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


    override fun onCleared() {
        super.onCleared()
        songPlayer.removeListener(playerListener)
        songPlayer.release()
    }
}
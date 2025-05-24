package itb.ac.id.purrytify.ui.onlinesong

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.data.model.OnlineSongResponse
import itb.ac.id.purrytify.data.repository.OnlineSongRepository
import itb.ac.id.purrytify.data.repository.UserRepository
import itb.ac.id.purrytify.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineSongViewModel @Inject constructor(
    private val onlineSongRepository: OnlineSongRepository,
    private val userRepository: UserRepository,
    private val songDao: SongDao,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _onlineSongs = MutableStateFlow<List<OnlineSongResponse>>(emptyList())
    private val _location = MutableStateFlow<String>("")
    val location: StateFlow<String> = _location
    val onlineSongs: StateFlow<List<OnlineSongResponse>> = _onlineSongs
    var isLoading by mutableStateOf(true)
        private set
    private lateinit var connectivityObserver: ConnectivityObserver
    private val _networkAvailable= MutableStateFlow<Boolean>(true)
    val networkAvailable: StateFlow<Boolean> = _networkAvailable

    init {
        observeNetworkConnectivity(appContext)
        observeUserLocation()

    }
    private fun observeUserLocation() {
        viewModelScope.launch {
            userRepository.location.collect { location ->
                _location.value = location
            }
        }

        viewModelScope.launch {
            _networkAvailable.collect { isAvailable ->
                if (isAvailable) {
                    Log.d("OnlineSongViewModel", "Network is available, checking location")

                    if (_location.value.isEmpty()) {
                        userRepository.getUserLocation()
                    }
                } else {
                    Log.d("OnlineSongViewModel", "Network is not available, skipping location fetch")
                }
            }
        }
    }

    fun fetchOnlineSongsGlobal() {
        viewModelScope.launch {
            try {
                isLoading = true
                val songs = onlineSongRepository.getOnlineSongGlobal()
                _onlineSongs.value = songs
                isLoading = false
            } catch (e: Exception) {
                // Handle error
                _onlineSongs.value = emptyList()
                Log.e("OnlineSongViewModel", "Error fetching online songs global", e)
            }
        }
    }

    fun fetchOnlineSongsCountry(country: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                val songs = onlineSongRepository.getOnlineSongCountry(country)
                _onlineSongs.value = songs
                isLoading = false
            } catch (e: Exception) {
                // Handle error
                _onlineSongs.value = emptyList()
                Log.e("OnlineSongViewModel", "Error fetching online songs country", e)
            }
        }
    }

    private fun observeNetworkConnectivity(context: Context) {
        connectivityObserver = NetworkConnectivityObserver(context)
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                _networkAvailable.value = status == ConnectionStatus.Available
                Log.d("Network", "Connection status: $status")
                Log.d ("Network", "Network available: ${_networkAvailable.value}")

            }
        }
    }

    fun downloadSong(context: Context, song: Song){
        viewModelScope.launch {
            val newSong = onlineSongRepository.downloadSongAndCover(context, song)
            Log.d("OnlineSongViewModel", "Song downloaded successfully: ${newSong.title}")
            Log.d("OnlineSongViewModel", "Song downloaded successfully: ${newSong.filePath}")
            Log.d("OnlineSongViewModel", "Song downloaded successfully: ${newSong.imagePath}")
            Log.d("OnlineSongViewModel", "Song downloaded successfully: $newSong")

            val id = songDao.insert(newSong)
            Log.d("OnlineSongViewModel", "Song inserted successfully: $id")
        }
    }

}
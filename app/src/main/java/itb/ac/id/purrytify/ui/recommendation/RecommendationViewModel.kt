package itb.ac.id.purrytify.ui.recommendation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.data.model.OnlineSongResponse
import itb.ac.id.purrytify.data.model.toSong
import itb.ac.id.purrytify.data.repository.OnlineSongRepository
import itb.ac.id.purrytify.data.repository.UserRepository
import itb.ac.id.purrytify.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val onlineSongRepository: OnlineSongRepository,
    private val userRepository: UserRepository,
    private val songDao: SongDao,
    private val tokenManager: TokenManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _listOfSongsOnline = MutableStateFlow<List<Song>>(emptyList())
    val listOfSongsOnline: StateFlow<List<Song>> = _listOfSongsOnline
    private val _listOfSongsLocal = MutableStateFlow<List<Song>>(emptyList())
    val listOfSongsLocal: StateFlow<List<Song>> = _listOfSongsLocal
    private lateinit var connectivityObserver: ConnectivityObserver
    private val _networkAvailable = MutableStateFlow(true)
    val networkAvailable: StateFlow<Boolean> = _networkAvailable
    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location
    var isLoading by mutableStateOf(true)
        private set

    init {
        observeNetworkConnectivity(appContext)
        observeUserLocation()
        fetchRecommendedSongsLocal()
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

    fun fetchRecommendedSongsOnline() {
        viewModelScope.launch {
            try {
                isLoading = true
                val onlineSongsGlobal = onlineSongRepository.getOnlineSongGlobal()
                val global10 = onlineSongsGlobal.take(10)
                val onlineSongsCountry = onlineSongRepository.getOnlineSongCountry(_location.value)
                val country10 = onlineSongsCountry.take(10)
                val onlineSongs = (global10 + country10).distinctBy { it.title to it.artist }
                val recommendedSongs = onlineSongs.map { onlineSong ->
                    onlineSong.toSong()
                }
                _listOfSongsOnline.value = recommendedSongs
                isLoading = false

            } catch (e: Exception) {
                Log.e("RecommendationViewModel", "Error fetching recommended songs: ${e.message}")
            }
        }
    }

    private fun fetchRecommendedSongsLocal() {
        viewModelScope.launch {
            try {
                isLoading = true
                val userID = tokenManager.getCurrentUserID()
                _listOfSongsLocal.value = songDao.getRecommended(userID)
                isLoading = false
            } catch (e: Exception) {
                Log.e("RecommendationViewModel", "Error fetching local songs: ${e.message}")
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
    fun clear() {
        _listOfSongsOnline.value = emptyList()
        isLoading = false
    }
}
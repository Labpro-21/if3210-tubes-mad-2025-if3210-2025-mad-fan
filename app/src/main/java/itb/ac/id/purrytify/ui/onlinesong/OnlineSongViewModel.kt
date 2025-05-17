package itb.ac.id.purrytify.ui.onlinesong

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.data.model.OnlineSongResponse
import itb.ac.id.purrytify.data.repository.OnlineSongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineSongViewModel @Inject constructor(
    private val onlineSongRepository: OnlineSongRepository,
    private val songDao: SongDao
) : ViewModel() {

    private val _onlineSongs = MutableStateFlow<List<OnlineSongResponse>>(emptyList())
    val onlineSongs: StateFlow<List<OnlineSongResponse>> = _onlineSongs
    var isLoading by mutableStateOf(true)
        private set
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
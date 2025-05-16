package itb.ac.id.purrytify.ui.onlinesong

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.model.OnlineSongResponse
import itb.ac.id.purrytify.data.repository.OnlineSongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineSongViewModel @Inject constructor(
    private val onlineSongRepository: OnlineSongRepository,
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
}
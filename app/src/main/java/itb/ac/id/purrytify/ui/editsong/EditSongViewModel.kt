package itb.ac.id.purrytify.ui.editsong
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditSongViewModel @Inject constructor (
    private val SongDao: SongDao,
    private val tokenManager: TokenManager
) : ViewModel(){
    fun saveOrUpdateSong(song: Song) {
        Log.d("EditSongViewModel", "Saving or updating song: $song")
        viewModelScope.launch {
            song.userID = tokenManager.getCurrentUserID()
            if (SongDao.getById(song.songId, song.userID) != null) {
                SongDao.update(song)
            } else {
                SongDao.insert(song)
            }
        }
    }

}
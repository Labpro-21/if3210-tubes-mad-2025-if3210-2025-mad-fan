package itb.ac.id.purrytify.ui.addsong
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSongViewModel @Inject constructor (
    private val SongDao: SongDao,
    private val tokenManager: TokenManager
) : ViewModel(){
    fun saveAddSong(song: Song){
        viewModelScope.launch{
            Log.d("All Songs", song.toString())
            song.userID = tokenManager.getCurrentUserID()
            Log.d("All Songs", song.toString() + " userID: " + tokenManager.getCurrentUserID())
            SongDao.insert(song)
            Log.d("All Songs", SongDao.getAll().toString())
        }
    }
}
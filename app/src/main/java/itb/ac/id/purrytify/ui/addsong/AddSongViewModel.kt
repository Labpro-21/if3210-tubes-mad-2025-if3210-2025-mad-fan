package itb.ac.id.purrytify.ui.addsong
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSongViewModel @Inject constructor (
    private val SongDao: SongDao,
) : ViewModel(){
//    var _songInserted = MutableStateFlow(false)
    fun saveAddSong(song: Song){
        viewModelScope.launch{
            Log.d("All Songs", song.toString())
            SongDao.insert(song)
//            _songInserted.value = true
            Log.d("All Songs", SongDao.getAll().toString())
        }
    }
//    fun resetInsertSignal(){
//        _songInserted.value = false
//    }
}
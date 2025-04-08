package itb.ac.id.purrytify.ui.addsong
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSongViewModel @Inject constructor (
    private val SongDao: SongDao,
) : ViewModel(){
    fun saveAddSong(song: Song){
        viewModelScope.launch{
            SongDao.insert(song)
        }
    }
}
package itb.ac.id.purrytify.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.local.dao.SongDao
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.data.repository.AuthRepository
import itb.ac.id.purrytify.data.repository.UserRepository
import itb.ac.id.purrytify.service.TokenCheckService
import itb.ac.id.purrytify.service.TokenCheckServiceScheduler
import itb.ac.id.purrytify.utils.ConnectivityObserver
import itb.ac.id.purrytify.utils.ConnectionStatus
import itb.ac.id.purrytify.utils.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import okhttp3.RequestBody.Companion.asRequestBody

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val id: Int? = null,
    val username: String? = null,
    val email: String? = null,
    val profilePhoto: String? = null,
    val location: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val songsCount: Int = 0,
    val likedCount: Int = 0,
    val listenedCount: Int = 0,
    val isNetworkAvailable: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val songDao: SongDao,
    ) : ViewModel() {
    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _editProfileState = MutableStateFlow<EditProfileState>(EditProfileState.Idle)
    val editProfileState: StateFlow<EditProfileState> = _editProfileState.asStateFlow()

    // Add logout state
    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    // Network status
    private val _networkStatus = MutableStateFlow(ConnectionStatus.Available)
    val networkStatus: StateFlow<ConnectionStatus> = _networkStatus.asStateFlow()

    // Songs Count
    private val _songsCount = MutableStateFlow<Int>(0)
    val songsCount: StateFlow<Int> = _songsCount

    private val _likedCount = MutableStateFlow<Int>(0)
    val likedCount: StateFlow<Int> = _likedCount

    private val _listenedCount = MutableStateFlow<Int>(0)
    val listenedCount: StateFlow<Int> = _listenedCount

    private lateinit var connectivityObserver: ConnectivityObserver

    fun updateProfile(location: String? = null, profilePhoto: Uri? = null, context: Context) {
        if (_networkStatus.value != ConnectionStatus.Available) {
            _editProfileState.value = EditProfileState.Error("No internet connection")
            return
        }

        viewModelScope.launch {
            try {
                _editProfileState.value = EditProfileState.Loading

                val photoFile: MultipartBody.Part? = profilePhoto?.let {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val file = File(context.cacheDir, "profile_photo.jpg")
                    inputStream?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("profilePhoto", file.name, requestFile)
                }

                // Buat request
                val locationPart = location?.let {
//                    log location
                    Log.d("ProfileViewModel", "Location: $it")
                    RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }

                val response = userRepository.updateProfile(locationPart, photoFile)

//                log response
                Log.d("ProfileViewModel", "Update Profile Response: $response")

                // Update profile w data baru
                _profileState.value = _profileState.value.copy(
                    profilePhoto = response.profilePhoto,
                    location = response.location
                )

                _editProfileState.value = EditProfileState.Success

                // Refresh profile
                fetchProfile()
            } catch (e: Exception) {
                _editProfileState.value = EditProfileState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun resetEditProfileState() {
        _editProfileState.value = EditProfileState.Idle
    }

    fun observeNetworkConnectivity(context: Context) {
        connectivityObserver = NetworkConnectivityObserver(context)
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                _networkStatus.value = status

//                Log.d("Network", "connection status pvm : ${ConnectionStatus.Available}" )
//                Log.d("Network", "connection status pvm : ${ConnectionStatus.Unavailable}" )
                _profileState.value = _profileState.value.copy(
                    isNetworkAvailable = status == ConnectionStatus.Available
                )
//                Log.d("Network", "isNetworkAvailable pvm : $status" )
                // Fetch profile kalau udah available
                if (status == ConnectionStatus.Available) {
                    fetchProfile()
                }
            }
        }
    }

    fun fetchProfile() {
        if (_networkStatus.value != ConnectionStatus.Available) {
            _profileState.value = _profileState.value.copy(
                isLoading = false,
                isNetworkAvailable = false
            )
            return
        }

        viewModelScope.launch {
            try {
                _profileState.value = _profileState.value.copy(isLoading = true)
                val profile = userRepository.getProfile()

                launch {
                    songDao.getAllCount(profile.id).collectLatest { count ->
                        _songsCount.value = count
                        _profileState.value = _profileState.value.copy(songsCount = count)
                    }
                }

                launch {
                    songDao.getLikedCount(profile.id).collectLatest { count ->
                        _likedCount.value = count
                        _profileState.value = _profileState.value.copy(likedCount = count)
                    }
                }

                launch {
                    songDao.getPlayedCount(profile.id).collectLatest { count ->
                        _listenedCount.value = count
                        _profileState.value = _profileState.value.copy(listenedCount = count)
                    }
                }

                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    id = profile.id,
                    username = profile.username,
                    email = profile.email,
                    profilePhoto = profile.profilePhoto,
                    location = profile.location,
                    createdAt = profile.createdAt,
                    updatedAt = profile.updatedAt,
                    isNetworkAvailable = true
                )
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun logout(context: Context) {
        if (_networkStatus.value != ConnectionStatus.Available) {
            _logoutState.value = LogoutState.Error("No internet connection")
            return
        }

        viewModelScope.launch {
            try {
                _logoutState.value = LogoutState.Loading
                authRepository.logout()
                TokenCheckServiceScheduler.cancelScheduleTokenCheck(context)
                _logoutState.value = LogoutState.Success
            } catch (e: Exception) {
                _logoutState.value = LogoutState.Error(e.message ?: "Logout failed")
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = LogoutState.Idle
    }
}

sealed class LogoutState {
    data object Idle : LogoutState()
    data object Loading : LogoutState()
    data object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}

sealed class EditProfileState {
    data object Idle : EditProfileState()
    data object Loading : EditProfileState()
    data object Success : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}

package itb.ac.id.purrytify.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.repository.AuthRepository
import itb.ac.id.purrytify.data.repository.UserRepository
import itb.ac.id.purrytify.utils.ConnectivityObserver
import itb.ac.id.purrytify.utils.ConnectionStatus
import itb.ac.id.purrytify.utils.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    // Add logout state
    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    // Network status
    private val _networkStatus = MutableStateFlow(ConnectionStatus.Available)
    val networkStatus: StateFlow<ConnectionStatus> = _networkStatus.asStateFlow()

    private lateinit var connectivityObserver: ConnectivityObserver

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
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    id = profile.id,
                    username = profile.username,
                    email = profile.email,
                    profilePhoto = profile.profilePhoto,
                    location = profile.location,
                    createdAt = profile.createdAt,
                    updatedAt = profile.updatedAt,
                    // TODO: Connect data actual
                    songsCount = 135, // Dummy, nanti connect ke actual
                    likedCount = 32,  // Dummy, nanti connect ke actual
                    listenedCount = 50, // Dummy, nanti connect ke actual
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
package itb.ac.id.purrytify.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.model.ProfileResponse
import itb.ac.id.purrytify.data.repository.UserRepository
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
    val listenedCount: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    fun fetchProfile() {
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
                    listenedCount = 50 // Dummy, nanti connect ke actual
                )
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load profile"
                )
            }
        }
    }
}
package itb.ac.id.purrytify.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val songsCount: Int = 135,  // Dummy data
    val likedCount: Int = 32,   // Dummy data
    val listenedCount: Int = 50 // Dummy data
)

class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    fun fetchProfile() {
        viewModelScope.launch {
            try {
                _profileState.value = _profileState.value.copy(isLoading = true)

                // Nanti replace API call
                // val profile = userRepository.getProfile()

                // Sekarang dummy dulu
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    id = 1,
                    username = "13522xxx",
                    email = "user@example.com",
                    profilePhoto = "dummy.png",
                    location = "Indonesia",
                    createdAt = "2023-01-01",
                    updatedAt = "2023-01-01"
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
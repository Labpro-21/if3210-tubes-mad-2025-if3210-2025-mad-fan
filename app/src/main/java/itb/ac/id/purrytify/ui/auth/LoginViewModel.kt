package itb.ac.id.purrytify.ui.auth

import android.content.Intent
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import itb.ac.id.purrytify.data.repository.AuthRepository
import itb.ac.id.purrytify.service.TokenCheckServiceScheduler
import itb.ac.id.purrytify.ui.PurrytifyApp
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private var _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            Log.d("Login", "Email: $email, Password: $password")
            Log.d("Login","Login result: $result")
            _loginState.value = if (result.isSuccess) {
                LoginState.Success
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    fun checkLogin(){
        viewModelScope.launch {
            // Check if the user is logged in then go to MainActivity
            if (authRepository.verifyToken().isSuccessful) {
                Log.d("MainActivity", "Token is valid")
                _loginState.value = LoginState.Success
            } else { // Token is invalid then refresh the token
                Log.d("MainActivity", "Token is invalid")
                if (authRepository.refreshToken().isSuccessful) {
                    // Token is refreshed successfully go to main
                    Log.d("MainActivity", "Token is refreshed")
                    _loginState.value = LoginState.Success
                } else {
                    Log.d("MainActivity", "Token refresh failed")
                    // Token refresh failed, go to login
                }
            }
        }
    }
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

}

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
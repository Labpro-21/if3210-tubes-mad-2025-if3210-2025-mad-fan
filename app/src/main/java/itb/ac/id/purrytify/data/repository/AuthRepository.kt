package itb.ac.id.purrytify.data.repository

import android.util.Log
import itb.ac.id.purrytify.data.api.ApiService
//import itb.ac.id.purrytify.data.api.RetrofitClient
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.model.LoginRequest
import itb.ac.id.purrytify.data.model.RefreshResponse
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { tokenManager.saveAccessToken(it.token) }
//                response.body()?.let { tokenManager.saveRefreshToken(it.refresh) }
//                tokenManager.setLoggedIn(true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
//        tokenManager.setLoggedIn(false)
    }

    suspend fun refreshToken(): Response<RefreshResponse> {
            val refreshToken = tokenManager.getRefreshToken()
            val response = authApi.refreshToken(refreshToken)
            if (response.isSuccessful) {
                response.body()?.let { tokenManager.saveAccessToken(it.token) }
                return response
            } else {
                return  response
            }
    }

    suspend fun verifyToken(): Response<Unit> {
        return authApi.verifyToken()
    }
//    suspend fun isLoggedIn(): Boolean {
//        return tokenManager.isLoggedIn()
//    }
}
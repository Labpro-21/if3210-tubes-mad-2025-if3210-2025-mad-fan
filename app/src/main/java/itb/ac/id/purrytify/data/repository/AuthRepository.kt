package itb.ac.id.purrytify.data.repository

import itb.ac.id.purrytify.data.api.ApiService
//import itb.ac.id.purrytify.data.api.RetrofitClient
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.model.LoginRequest
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
                response.body()?.let { tokenManager.saveRefreshToken(it.refresh) }
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
    }

    suspend fun refreshToken(): Result<Unit> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            val response = authApi.refreshToken(refreshToken)
            if (response.isSuccessful) {
                response.body()?.let { tokenManager.saveAccessToken(it.token) }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Refresh token failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyToken(): Result<Unit> {
        return try {
            val response = authApi.verifyToken()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Token verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
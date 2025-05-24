package itb.ac.id.purrytify.data.repository

import android.util.Log
import com.auth0.jwt.JWT
import itb.ac.id.purrytify.data.api.ApiService
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.model.*
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
                response.body()?.let { tokenManager.saveRefreshToken(it.refresh) }
                val decodedJWT = JWT.decode(response.body()?.token)
                val userID = decodedJWT.getClaim("id").asInt()
                tokenManager.saveCurrentUserID(userID)
                Result.success(Unit)
            } else {
                Log.e("AuthRepository", "Login failed with code: ${response.code()}")
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Execoption during login: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }

    suspend fun refreshToken(): Response<RefreshResponse> {
        val refreshRequest = RefreshRequest(tokenManager.getRefreshToken())
        Log.d("AuthRepository", "Refresh token: ${refreshRequest.refreshToken}")
        val response = authApi.refreshToken(refreshRequest)
        Log.d("AuthRepository", "Refresh token response: ${response.code()}")
        Log.d("AuthRepository", "Refresh token response: ${response.body()}")
        if (response.isSuccessful) {
            response.body()?.let { tokenManager.saveAccessToken(it.accessToken) }
            response.body()?.let { tokenManager.saveRefreshToken(it.refreshToken) }
            return response
        } else {
            return  response
        }
    }

    suspend fun verifyToken(): Response<VerifyResponse> {
        return authApi.verifyToken()
    }
}
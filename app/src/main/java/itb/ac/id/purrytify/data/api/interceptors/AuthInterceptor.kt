package itb.ac.id.purrytify.data.api.interceptors

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking {
            tokenManager.getAccessToken()
        }
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        Log.d("AuthInterceptor", "Access token: $accessToken")
        return try {
            val response = chain.proceed(request)

            if (response.code == 401) {
                Log.e("AuthInterceptor", "Token expired, refreshing token")
            }

            response
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Network error: ${e.localizedMessage}", e)

            // Rethrow the exception to let the repository/viewmodel catch it
            throw e
        }
    }
}
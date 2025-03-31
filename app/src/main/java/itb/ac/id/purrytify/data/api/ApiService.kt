package itb.ac.id.purrytify.data.api

import itb.ac.id.purrytify.data.model.LoginRequest
import itb.ac.id.purrytify.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

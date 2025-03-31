package itb.ac.id.purrytify.data.api

import itb.ac.id.purrytify.data.model.LoginRequest
import itb.ac.id.purrytify.data.model.LoginResponse
import itb.ac.id.purrytify.data.model.ProfileResponse
import itb.ac.id.purrytify.data.model.RefreshResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @GET("uploads/profile-picture/{profilePhotoPath}")
    suspend fun getProfilePicture(@Path("profilePhotoPath") profilePhotoPath: String): Response<ResponseBody>

    @GET("api/verify-token")
    suspend fun verifyToken(): Response<Unit>

    @POST("api/refresh-token")
    suspend fun refreshToken(@Body refreshToken: String): Response<RefreshResponse>

}

package itb.ac.id.purrytify.data.api

import itb.ac.id.purrytify.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PATCH
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @GET("uploads/profile-picture/{profilePhotoPath}")
    suspend fun getProfilePicture(@Path("profilePhotoPath") profilePhotoPath: String): Response<ResponseBody>

    @Multipart
    @PATCH("api/profile")
    suspend fun updateProfile(
        @Part("location") location: RequestBody? = null,
        @Part profilePhoto: MultipartBody.Part? = null
    ): Response<ProfileResponse>

    @GET("api/verify-token")
    suspend fun verifyToken(): Response<VerifyResponse>

    @POST("api/refresh-token")
    suspend fun refreshToken(@Body refreshToken: RefreshRequest): Response<RefreshResponse>

    @GET("api/top-songs/global")
    suspend fun getTopSongsGlobal(): Response<List<OnlineSongResponse>>

    @GET("api/top-songs/{countryCode}")
    suspend fun getTopSongsCountry(@Path("countryCode") countryCode: String): Response<List<OnlineSongResponse>>

    @GET("api/songs/{id}")
    suspend fun getOnlineSongById(@Path("id") id: String): Response<OnlineSongResponse>
}

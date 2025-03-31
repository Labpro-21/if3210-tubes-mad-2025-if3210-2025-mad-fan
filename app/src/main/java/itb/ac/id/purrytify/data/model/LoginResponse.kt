package itb.ac.id.purrytify.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("accessToken") val token: String,
    @SerializedName("refreshToken") val refresh: String
)
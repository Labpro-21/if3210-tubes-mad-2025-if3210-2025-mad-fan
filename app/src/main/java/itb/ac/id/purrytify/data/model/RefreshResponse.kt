package itb.ac.id.purrytify.data.model

import com.google.gson.annotations.SerializedName

data class RefreshResponse(
    @SerializedName("accessToken") val token: String,
)

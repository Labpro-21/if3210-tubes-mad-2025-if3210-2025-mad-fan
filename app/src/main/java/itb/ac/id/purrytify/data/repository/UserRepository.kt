package itb.ac.id.purrytify.data.repository
import itb.ac.id.purrytify.data.api.ApiService
import itb.ac.id.purrytify.data.api.RetrofitClient
import itb.ac.id.purrytify.data.model.ProfileResponse

class UserRepository{
    private val apiService: ApiService = RetrofitClient.api
    suspend fun getProfile(): ProfileResponse {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                response.body()?.let {
                    ProfileResponse(
                        id = it.id,
                        username = it.username,
                        email = it.email,
                        profilePhoto = it.profilePhoto,
                        location = it.location,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                } ?: throw Exception("Profile not found")
            } else {
                throw Exception("Failed to get profile")
            }
        } catch (e: Exception) {
            throw e
        }
    }
    suspend fun getProfilePicture(profilePhotoPath: String) : ByteArray {
        return try {
            val response = apiService.getProfilePicture(profilePhotoPath)
            if (response.isSuccessful) {
                response.body()?.bytes() ?: throw Exception("Profile picture not found")
            } else {
                throw Exception("Failed to get profile picture")
            }
        } catch (e: Exception) {
            throw e
        }
    }
}
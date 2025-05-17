package itb.ac.id.purrytify.data.repository
import itb.ac.id.purrytify.data.api.ApiService
import itb.ac.id.purrytify.data.model.ProfileResponse
import javax.inject.Inject
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepository @Inject constructor(
    private val apiService: ApiService
){
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
    suspend fun updateProfile(
        location: RequestBody? = null,
        profilePhoto: MultipartBody.Part? = null
    ): ProfileResponse {
        return try {
            val response = apiService.updateProfile(location, profilePhoto)
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
                throw Exception("Failed to update profile")
            }
        } catch (e: Exception) {
            throw e
        }
    }
}
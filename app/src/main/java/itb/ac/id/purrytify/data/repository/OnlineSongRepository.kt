package itb.ac.id.purrytify.data.repository

import itb.ac.id.purrytify.data.api.ApiService
import itb.ac.id.purrytify.data.model.OnlineSongResponse
import javax.inject.Inject

class OnlineSongRepository @Inject constructor(
    private val apiService: ApiService
){
    suspend fun getOnlineSongGlobal(): List<OnlineSongResponse> {
        return try {
            val response = apiService.getTopSongsGlobal()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOnlineSongCountry(country: String): List<OnlineSongResponse> {
        return try {
            val response = apiService.getTopSongsCountry(country)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
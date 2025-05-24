package itb.ac.id.purrytify.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import itb.ac.id.purrytify.data.api.ApiService
import itb.ac.id.purrytify.data.api.interceptors.TokenManager
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.data.model.OnlineSongResponse
import itb.ac.id.purrytify.di.UnauthenticatedClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import javax.inject.Inject

class OnlineSongRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    @UnauthenticatedClient private val client: OkHttpClient
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

    suspend fun getOnlineSongById(id: String): OnlineSongResponse? {
        return try {
            val response = apiService.getOnlineSongById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveOnlineSongToExternalStorage(
        context: Context,
        fileUrl: String,
        fileName: String,
        mimeType: String
    ): Uri {

        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(fileUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Download failed: $fileUrl")

            val folderPath = when {
                mimeType.startsWith("audio") -> "Music/Purrytify"
                mimeType.startsWith("image") -> "Pictures/Purrytify/Cover"
                else -> "Download/Purrytify/Misc"
            }

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val collection = when {
                mimeType.startsWith("audio") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                mimeType.startsWith("image") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Files.getContentUri("external")
            }

            val uri = resolver.insert(collection, contentValues)
                ?: throw IOException("Failed to create media entry")

            resolver.openOutputStream(uri)?.use { output ->
                response.body?.byteStream()?.copyTo(output)
            }

            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            uri
        }
    }

    private suspend fun saveOnlineSongToInternalStorage(
        context: Context,
        fileUrl: String,
        fileName: String,
        mimeType: String
    ): Uri {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(fileUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Download failed: $fileUrl")

            // Choose subfolder inside internal storage
            val subfolder = when {
                mimeType.startsWith("audio") -> "music_purrytify"
                mimeType.startsWith("image") -> "images_purrytify"
                else -> "misc_purrytify"
            }

            val directory = File(context.filesDir, subfolder).apply {
                if (!exists()) mkdirs()
            }

            val file = File(directory, fileName)

            response.body?.byteStream()?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Uri.fromFile(file)
        }
    }


    suspend fun downloadSongAndCover(
        context: Context,
        song: Song
    ): Song {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Downloading ${song.title}", Toast.LENGTH_SHORT).show()
        }
        return withContext(Dispatchers.IO) {

            val songUri = saveOnlineSongToInternalStorage(context, song.filePath, song.title, "audio/mpeg")
            val imageUri = saveOnlineSongToInternalStorage(context, song.imagePath, "${song.title}_cover", "image/jpeg")

            return@withContext song.copy(
                songId = 0,
                userID = tokenManager.getCurrentUserID(),
                filePath = songUri.toString(),
                imagePath = imageUri.toString(),
                isDownloaded = true,
                isOnline = false,
                createdAt = System.currentTimeMillis()
            )
        }.also {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Downloaded ${it.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
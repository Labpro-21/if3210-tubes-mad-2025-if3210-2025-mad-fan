package itb.ac.id.purrytify.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import itb.ac.id.purrytify.data.local.entity.Song

class AddSongUtil{
    companion object{
        fun extractMetadata(context: Context, uri: Uri): Song? {
            val retriever = MediaMetadataRetriever()
            return try {
                retriever.setDataSource(context, uri)
                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown Title"
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                val filePath = uri.toString()
                val imagePath = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: "Unknown Image Path"
                Song(
                    title = title,
                    artist = artist,
                    filePath = filePath,
                    imagePath = imagePath,
                    duration = duration
                )
            }
            catch (e: Exception) {
                Log.e("AddSongUtil", "Error extracting metadata: ${e.message}")
                null
            } finally {
                retriever.release()
            }
        }
        fun getFileName(context: Context, uri: Uri): String {
            val contentResolver = context.contentResolver
            val projection = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME)

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    return cursor.getString(nameIndex)
                }
            }

            // Fallback if query fails
            return uri.lastPathSegment?.substringAfterLast('/') ?: "Unknown File Name"
        }

    }


}
package itb.ac.id.purrytify.data.model

import itb.ac.id.purrytify.data.local.entity.Song

data class OnlineSongResponse(
    val id: Int,
    val title: String,
    val artist: String,
    val artwork: String,
    val url: String,
    val duration: String, // format: mm:ss
    val country: String,
    val rank: Int,
    val createdAt: String,
    val updatedAt: String
)

fun OnlineSongResponse.toSong(userId: Int = -1): Song {
    return Song(
        title = title,
        artist = artist,
        filePath = url, // online url instead of uri
        imagePath = artwork,
        userID = userId,
        duration = duration.toMillis(), // parse mm:ss
        isLiked = false,
        isOnline = true,
    )
}

fun String.toMillis(): Long {
    val parts = this.split(":")
    return try {
        val minutes = parts.getOrNull(0)?.toLongOrNull() ?: 0L
        val seconds = parts.getOrNull(1)?.toLongOrNull() ?: 0L
        (minutes * 60 + seconds) * 1000
    } catch (e: Exception) {
        0L
    }
}

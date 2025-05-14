package itb.ac.id.purrytify.data.model

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

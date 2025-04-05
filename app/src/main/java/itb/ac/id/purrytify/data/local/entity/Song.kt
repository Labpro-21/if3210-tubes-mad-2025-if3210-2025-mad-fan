package itb.ac.id.purrytify.data.local.entity

import androidx.room.*

@Entity(tableName = "song")
data class Song (
    @PrimaryKey val songId: Int,
    val title: String,
    val artist: String,
    val filePath: String,
    val imagePath: String,
    val duration: Int,
)



package itb.ac.id.purrytify.data.local.entity

import androidx.room.*

@Entity(tableName = "song")
data class Song (
    @PrimaryKey(autoGenerate = true) val songId: Int = 0,
    var title: String,
    var artist: String,
    var filePath: String,
    var imagePath: String,
    var duration: Long = 0L,
    var userID: Int,
    var isLiked: Boolean = false,
)



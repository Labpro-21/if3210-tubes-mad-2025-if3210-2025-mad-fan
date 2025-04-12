//package itb.ac.id.purrytify.data.local.entity
//
//import androidx.room.*
//
//@Entity(tableName = "liked_song",
//    foreignKeys = [ForeignKey(
//        entity = Song::class,
//        parentColumns = ["songId"],
//        childColumns = ["songId"],
//        onDelete = ForeignKey.CASCADE
//    )],)
//data class LikedSong (
//    @PrimaryKey val songId: Int,
//)
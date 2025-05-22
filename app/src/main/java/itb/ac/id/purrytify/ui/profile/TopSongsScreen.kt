package itb.ac.id.purrytify.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import itb.ac.id.purrytify.R

data class TopSong(
    val rank: Int,
    val title: String,
    val artist: String,
    val albumName: String,
    val plays: Int,
    val imageId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSongsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // dummy
    val topSongs = listOf(
        TopSong(1, "Starboy", "The Weeknd", "Daft Punk", 15, R.drawable.profile_dummy),
        TopSong(2, "Loose", "Daniel Caesar", "", 12, R.drawable.profile_dummy),
        TopSong(3, "Nights", "Frank Ocean", "Blond", 8, R.drawable.profile_dummy),
        TopSong(4, "Doomsday", "MF DOOM", "Pebbles The Invisible Girl", 4, R.drawable.profile_dummy),
        TopSong(5, "Self Control", "Frank Ocean", "Blond", 3, R.drawable.profile_dummy),
        TopSong(6, "Come Through and Chill", "Miguel", "War & Leisure", 2, R.drawable.profile_dummy),
        TopSong(7, "Golden", "Jill Scott", "The Light of the Sun", 2, R.drawable.profile_dummy),
        TopSong(8, "Earned It", "The Weeknd", "Fifty Shades of Grey", 1, R.drawable.profile_dummy),
        TopSong(9, "Pyramids", "Frank Ocean", "Channel Orange", 1, R.drawable.profile_dummy),
        TopSong(10, "The Hills", "The Weeknd", "Beauty Behind the Madness", 1, R.drawable.profile_dummy)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Top songs",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            )
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "April 2025",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    text = "You played ",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Text(
                    text = "203 different songs",
                    color = Color(0xFFFFEB3B),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
            }
            Text(
                text = "this month.",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // list songs
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(topSongs) { index, song ->
                Column {
                    TopSongItem(
                        song = song,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    if (index < topSongs.size - 1) {
                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopSongItem(
    song: TopSong,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // rank
        Text(
            text = String.format("%02d", song.rank),
            color = Color(0xFFFFEB3B),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            val subtitle = if (song.albumName.isNotEmpty()) {
                "${song.artist}, ${song.albumName}"
            } else {
                song.artist
            }

            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(
                    text = "${song.plays}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " plays",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Image(
            painter = painterResource(id = song.imageId),
            contentDescription = "Album art for ${song.title}",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopSongsScreenPreview() {
    TopSongsScreen()
}
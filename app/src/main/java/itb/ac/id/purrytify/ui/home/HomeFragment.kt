package itb.ac.id.purrytify.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme

@Composable
fun HomeFragment() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeContent()
    }
}

@Composable
fun HomeContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            text = "New songs",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // New Songs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(newSongsData) { song ->
                NewSongItem(song)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Recently played",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Recently played
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            recentlyPlayedData.forEach { song ->
                RecentlyPlayedItem(song)
            }
        }

        // Buat miniplayer & navigation nanti
        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
fun NewSongItem(song: Song) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { /* Implement nanti */ },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cover
        Image(
            painter = painterResource(id = song.coverResId),
            contentDescription = "${song.title} cover",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Judul
        Text(
            text = song.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Artis
        Text(
            text = song.artist,
            color = MaterialTheme.colorScheme.tertiary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RecentlyPlayedItem(song: Song) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Implement nanti */ }
    ) {
        // Cover
        Image(
            painter = painterResource(id = song.coverResId),
            contentDescription = "${song.title} cover",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))


        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Judul
            Text(
                text = song.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Artis
            Text(
                text = song.artist,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Data class dummy
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val coverResId: Int
)

// Masih dummy data, nanti diganti
val newSongsData = listOf(
    Song("1", "Starboy", "The Weeknd, Daft Punk", R.drawable.cover_starboy),
    Song("2", "Here Comes The Sun", "The Beatles", R.drawable.cover_abbey_road),
    Song("3", "Midnight Pretenders", "Tomoko Aran", R.drawable.cover_midnight_pretenders),
    Song("4", "Violent Crimes", "Kanye West", R.drawable.cover_ye)
)

val recentlyPlayedData = listOf(
    Song("5", "Jazz is for ordinary people", "berlioz", R.drawable.cover_jazz),
    Song("6", "Loose", "Daniel Caesar", R.drawable.cover_loose),
    Song("7", "Nights", "Frank Ocean", R.drawable.cover_blonde),
    Song("8", "Kiss of Life", "Sade", R.drawable.cover_sade),
    Song("9", "BEST INTEREST", "Tyler, The Creator", R.drawable.cover_best_interest)
)

@Preview(showBackground = true)
@Composable
fun HomeFragmentPreview() {
    PurrytifyTheme {
        HomeFragment()
    }
}
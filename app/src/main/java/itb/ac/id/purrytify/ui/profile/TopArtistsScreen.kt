package itb.ac.id.purrytify.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import itb.ac.id.purrytify.R

data class TopArtist(
    val rank: Int,
    val name: String,
    val imageId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopArtistsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // dummy
    val topArtists = listOf(
        TopArtist(1, "Beatles", R.drawable.profile_dummy),
        TopArtist(2, "The Weeknd", R.drawable.profile_dummy),
        TopArtist(3, "Kanye West", R.drawable.profile_dummy),
        TopArtist(4, "Doechii", R.drawable.profile_dummy),
        TopArtist(5, "Daniel Caesar", R.drawable.profile_dummy),
        TopArtist(6, "Frank Ocean", R.drawable.profile_dummy),
        TopArtist(7, "Tyler, The Creator", R.drawable.profile_dummy),
        TopArtist(8, "Kendrick Lamar", R.drawable.profile_dummy),
        TopArtist(9, "SZA", R.drawable.profile_dummy),
        TopArtist(10, "Mac Miller", R.drawable.profile_dummy)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Top artists",
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
                    text = "You listened to ",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Text(
                    text = "137 artists",
                    color = Color(0xFF2196F3),
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

        // list artist
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(topArtists) { index, artist ->
                Column {
                    TopArtistItem(
                        artist = artist,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    if (index < topArtists.size - 1) {
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
fun TopArtistItem(
    artist: TopArtist,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // rank
        Text(
            text = String.format("%02d", artist.rank),
            color = Color(0xFF2196F3),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = artist.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(id = artist.imageId),
            contentDescription = "Artist ${artist.name}",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopArtistsScreenPreview() {
    TopArtistsScreen()
}
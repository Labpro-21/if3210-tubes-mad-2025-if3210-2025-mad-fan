package itb.ac.id.purrytify.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import itb.ac.id.purrytify.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ProfileScreen()
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = hiltViewModel()
    val profileState by viewModel.profileState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }
//    Log.d("ProfileScreen", "ProfileScreen composable called")

    ProfileContent(profileState)
}

@Composable
fun ProfileContent(profileState: ProfileUiState) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Bg gradasi
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF00667B), MaterialTheme.colorScheme.background)
                    )
                )
        )

        if (profileState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
//        } else if (profileState.error != null) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Text(
//                    text = "Error: ${profileState.error}",
//                    color = Color.Red
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(
//                    onClick = { /* TODO handle klik retry, tapi sepertinya ga perlu */ },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color.DarkGray,
//                        contentColor = Color.White
//                    )
//                ) {
//                    Text("Retry")
//                }
//            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile picture
                    Box {
                        // Load profile image kalau ada, kalau ngga, dummy
                        if (profileState.profilePhoto != null) {
                            val context = LocalContext.current
                            val imageUrl = "http://34.101.226.132:3000/uploads/profile-picture/${profileState.profilePhoto}"
                            val painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .transformations(CircleCropTransformation())
                                    .error(R.drawable.profile_dummy)
                                    .placeholder(R.drawable.profile_dummy)
                                    .build()
                            )

                            Image(
                                painter = painter,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.profile_dummy),
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Button(
                            onClick = { /* TODO handle klik edit */ },
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.BottomEnd),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit Profile",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Username & location
                    Text(
                        text = profileState.username ?: "Username",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = profileState.location ?: "Location",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Edit button
                    Button(
                        onClick = { /* TODO handle klik edit */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .width(118.dp)
                    ) {
                        Text(
                            "Edit Profile",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }

                    Button(
                        onClick = { /* TODO handle klik edit */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .width(118.dp)
                    ) {
                        Text(
                            "Logout",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    // Stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(count = profileState.songsCount.toString(), label = "SONGS")
                        StatItem(count = profileState.likedCount.toString(), label = "LIKED")
                        StatItem(count = profileState.listenedCount.toString(), label = "LISTENED")
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = label,
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 3.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val dummyProfileState = ProfileUiState(
        username = "13522001",
        location = "Indonesia",
        songsCount = 135,
        likedCount = 32,
        listenedCount = 50
    )
    ProfileContent(profileState = dummyProfileState)
}
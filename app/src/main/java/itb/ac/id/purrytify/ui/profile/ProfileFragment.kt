package itb.ac.id.purrytify.ui.profile

import android.os.Bundle
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
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.ui.profile.ProfileUiState

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
                    ProfileScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val profileState by viewModel.profileState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    ProfileContent(profileState)
}

@Composable
fun ProfileContent(profileState: ProfileUiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto profil
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.profile_dummy),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Button(
                        onClick = { /* TO DO handle klik edit */ },
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
                    text = profileState.username ?: "13522xxx",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = profileState.location ?: "Indonesia",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol edit
                Button(
                    onClick = { /* TO DO handle klik edit */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Edit Profile",
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
                    StatItem(count = "135", label = "SONGS")
                    StatItem(count = "32", label = "LIKED")
                    StatItem(count = "50", label = "LISTENED")
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
        location = "Indonesia"
    )

    ProfileContent(profileState = dummyProfileState)
}

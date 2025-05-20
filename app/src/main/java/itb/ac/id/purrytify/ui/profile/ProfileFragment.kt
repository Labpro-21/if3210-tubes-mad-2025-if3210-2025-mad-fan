package itb.ac.id.purrytify.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import itb.ac.id.purrytify.ui.auth.LoginActivity
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme

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
                PurrytifyTheme {
                    val context = LocalContext.current
                    LaunchedEffect(Unit) {
                        viewModel.observeNetworkConnectivity(context)
                    }
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
    val logoutState by viewModel.logoutState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.observeNetworkConnectivity(context)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    // Logout sukses
    LaunchedEffect(logoutState) {
        when (logoutState) {
            is LogoutState.Success -> {
                Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                // Navigate ke login
                val intent = Intent(context, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
                viewModel.resetLogoutState()
                if (context is Activity) {
                    context.finish()
                }
            }
            else -> {}
        }
    }

    // Check network, tampilin halaman yang sesuai
//    Log.d("Network", "Status : ${profileState.isNetworkAvailable}" )
    if (!profileState.isNetworkAvailable) {
        NoInternetScreen(
            onRetryClick = { viewModel.fetchProfile() }
        )
    } else {
        ProfileContent(
            profileState = profileState,
            logoutState = logoutState,
            viewModel = viewModel
        )
    }
}

@Composable
fun ProfileContent(
    profileState: ProfileUiState,
    logoutState: LogoutState,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var showEditProfile by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    // Profile picture
                    Box {
                        // Load profile image kalau ada, kalau ngga, dummy
                        if (profileState.profilePhoto != null) {
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
                        onClick = { showEditProfile = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(horizontal = 1.dp, vertical = 4.dp)
                            .width(150.dp)
                    ) {
                        Text(
                            "Edit Profile",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.logout(context.applicationContext)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .width(150.dp)
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

                    Spacer(modifier = Modifier.height(32.dp))
                    SoundCapsuleSection()
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Popup
        if (showEditProfile) {
            EditProfileScreen(
                viewModel = viewModel,
                profileState = profileState,
                onDismiss = { showEditProfile = false }
            )
        }

        // Loading dan error dialog
        when (logoutState) {
            is LogoutState.Loading ->
                Dialog(onDismissRequest = {}) {
                    Surface(
                        modifier = Modifier
                            .size(150.dp)
                            .padding(8.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = Color.White,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            is LogoutState.Error -> {
                val message = (logoutState as LogoutState.Error).message
                AlertDialog(
                    onDismissRequest = { viewModel.resetLogoutState() },
                    title = { Text("Logout Failed") },
                    text = { Text(message) },
                    confirmButton = {
                        Button(onClick = { viewModel.resetLogoutState() }) {
                            Text("OK")
                        }
                    }
                )
            }
            else -> {}
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
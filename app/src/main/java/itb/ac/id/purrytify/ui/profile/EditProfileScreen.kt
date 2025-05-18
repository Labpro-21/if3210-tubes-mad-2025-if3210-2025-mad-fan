package itb.ac.id.purrytify.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import itb.ac.id.purrytify.R

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

import android.location.Geocoder
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import android.os.Looper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    profileState: ProfileUiState,
    onDismiss: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCountryCode by remember { mutableStateOf<String?>(profileState.location) }
    var selectedCountryName by remember {
        mutableStateOf(
            profileState.location?.let { CountryList.getCountryNameByCode(it) } ?: "Select Location"
        )
    }
    var showLocationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val editProfileState by viewModel.editProfileState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            selectedImageUri = photoUri
        } else {
            // Gagal
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestNewLocation(context: Context, onCountryCodeFound: (String?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
            numUpdates = 1
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val countryCode = addresses[0].countryCode
                            onCountryCodeFound(countryCode)
                        } else {
                            Toast.makeText(context, "Could not determine location", Toast.LENGTH_SHORT).show()
                            onCountryCodeFound(null)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
                        onCountryCodeFound(null)
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                    return
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to request location updates: ${e.message}", Toast.LENGTH_SHORT).show()
            onCountryCodeFound(null)
        }
    }

    fun getLocationAndSetCountry(context: Context, onCountryCodeFound: (String?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
            onCountryCodeFound(null)
            return
        }
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        try {
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                val countryCode = addresses[0].countryCode
                                onCountryCodeFound(countryCode)
                            } else {
                                Toast.makeText(context, "Could not determine location", Toast.LENGTH_SHORT).show()
                                onCountryCodeFound(null)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
                            onCountryCodeFound(null)
                        }
                    } else {
                        requestNewLocation(context, onCountryCodeFound)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                    onCountryCodeFound(null)
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Error accessing location: ${e.message}", Toast.LENGTH_SHORT).show()
            onCountryCodeFound(null)
        }
    }

    fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            image
        )
    }

    // Izin
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            getLocationAndSetCountry(context) { countryCode ->
                selectedCountryCode = countryCode
                selectedCountryName = CountryList.getCountryNameByCode(countryCode ?: "")
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoUri = createImageFile()
            photoUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    if (showLocationDialog) {
        LocationSelectionDialog(
            onDismiss = { showLocationDialog = false },
            onLocationSelected = { country ->
                selectedCountryCode = country.code
                selectedCountryName = country.name
                showLocationDialog = false
            },
            onAutoDetect = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                showLocationDialog = false
            }
        )
    }

    LaunchedEffect(editProfileState) {
        when (editProfileState) {
            is EditProfileState.Success -> {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetEditProfileState()
                onDismiss()
            }
            is EditProfileState.Error -> {
                Toast.makeText(
                    context,
                    (editProfileState as EditProfileState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetEditProfileState()
            }
            else -> {}
        }
    }

    // Popup edit profile
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Photo section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(selectedImageUri)
                                            .crossfade(true)
                                            .transformations(CircleCropTransformation())
                                            .build()
                                    ),
                                    contentDescription = "Selected Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            profileState.profilePhoto != null -> {
                                val imageUrl = "http://34.101.226.132:3000/uploads/profile-picture/${profileState.profilePhoto}"
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUrl)
                                            .crossfade(true)
                                            .transformations(CircleCropTransformation())
                                            .error(R.drawable.profile_dummy)
                                            .placeholder(R.drawable.profile_dummy)
                                            .build()
                                    ),
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Image(
                                    painter = painterResource(id = R.drawable.profile_dummy),
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "Profile Photo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Open Gallery
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Open Gallery"
                                )
                                Text("Open Gallery")
                            }
                        }

                        // Open Camera
                        Button(
                            onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Open Camera"
                                )
                                Text("Open Camera")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Location
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = selectedCountryName,
                                onValueChange = {},
                                readOnly = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )

                            // Edit location
                            IconButton(
                                onClick = { showLocationDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Location",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Save
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            location = selectedCountryCode,
                            profilePhoto = selectedImageUri,
                            context = context
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = selectedImageUri != null || selectedCountryCode != profileState.location
                ) {
                    Text(
                        text = "Save Changes",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (editProfileState is EditProfileState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
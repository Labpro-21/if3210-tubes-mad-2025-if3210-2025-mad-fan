package itb.ac.id.purrytify.ui.addsong

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.data.local.entity.Song
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme
import itb.ac.id.purrytify.utils.AddSongUtil.Companion.extractMetadata
import itb.ac.id.purrytify.utils.AddSongUtil.Companion.getFileName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongScreen(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    viewModel: AddSongViewModel? = null
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var songFileName by remember { mutableStateOf("Upload File") }

    var songUri by remember { mutableStateOf<Uri?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var song by remember { mutableStateOf<Song?>(null) }

    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            photoUri = it
        }
    }

    val audioPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            songUri = it
        }
    }

    LaunchedEffect(songUri) {
        songUri?.let {
            song = extractMetadata(context, it)
            title = song?.title ?: ""
            artist = song?.artist ?: ""
            songFileName = getFileName(context, it)
        }
    }

    LaunchedEffect(photoUri) {
        photoUri?.let {
            song?.imagePath = it.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Upload Song",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                UploadBoxFile(songFileName) {
                    audioPicker.launch(arrayOf("audio/*"))
                }

                UploadBoxPhoto(selectedImage = photoUri) {
                    photoPicker.launch(arrayOf("image/*"))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Title",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
            )
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Artist",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            )
            TextField(
                value = artist,
                onValueChange = { artist = it },
                placeholder = { Text("Artist") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                )
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(0.4f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(text = "Cancel")
                }

                Spacer(modifier = Modifier.width(24.dp))

                Button(
                    onClick = {
                        song?.let {
                            it.title = title
                            it.artist = artist
                        }
                        if (songUri != null && photoUri != null) {
                            viewModel?.saveAddSong(song!!)
                            onSave()
                        } else {
                            Toast.makeText(context, "Please Upload a Song and Artwork", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(0.4f)
                ) {
                    Text(text = "Save")
                }
            }
        }
    }
}


@Composable
fun UploadBoxFile (title:String = "Upload File", onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ){
        Box(
            modifier = Modifier
                    .drawBehind {
                    drawRoundRect(
                        color = Color(0xff535353),
                        size = size,
                        cornerRadius = CornerRadius(10f, 10f),
                        style = Stroke(width = 5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        ))
                    }
                    .size(110.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
            )
            Image(
                painter = painterResource(id = R.drawable.upload_file),
                contentDescription = "Upload Icon",
                modifier = Modifier
                    .size(80.dp)
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(20))
                .size(16.dp)
        ){
            Image(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = "Edit Icon",
                modifier = Modifier
                    .size(14.dp)
                    .padding(2.dp)
                    .align(Alignment.Center),

                )
        }
    }
}

@Composable
fun UploadBoxPhoto (selectedImage: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ){
        if (selectedImage == null) {
            Box(
                modifier = Modifier
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0xff535353),
                            size = size,
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            ))
                    }
                    .size(110.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(if (selectedImage == null) 16.dp else 0.dp)
        ) {
            if (selectedImage == null) {
                Text(
                    text = "Upload Photo",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom=5.dp),
                )
                Image(
                    painter = painterResource(id = R.drawable.upload_photo),
                    contentDescription = "Upload Icon",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally),
                )
            } else {
                AsyncImage(
                    model = selectedImage,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(120.dp, 120.dp)
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(20))
                    .size(16.dp)
            ){
                Image(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Edit Icon",
                    modifier = Modifier
                        .size(14.dp)
                        .padding(2.dp)
                        .align(Alignment.Center),

                    )
            }
    }
}

@Composable
@Preview(showBackground = true)
fun UploadBoxPreview() {
    PurrytifyTheme {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
            UploadBoxFile("Upload File"){}
            UploadBoxPhoto(null){}
        }
    }
}

@Composable
@Preview(showBackground = true)
fun AddSongScreenPreview() {
    var showSheet by remember { mutableStateOf(true) }

    PurrytifyTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showSheet = false }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (showSheet) {
                    AddSongScreen(
                        onDismiss = { showSheet = false },
                        onSave = { showSheet = false },
                    )
                }
            }
        }
    }
}
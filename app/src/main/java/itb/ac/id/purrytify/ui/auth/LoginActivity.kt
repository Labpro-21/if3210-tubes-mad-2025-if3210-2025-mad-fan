package itb.ac.id.purrytify.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import itb.ac.id.purrytify.R
import itb.ac.id.purrytify.service.TokenCheckServiceScheduler
import itb.ac.id.purrytify.ui.MainActivity
import itb.ac.id.purrytify.ui.theme.PurrytifyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        var keepSplashScreen = true
        super.onCreate(savedInstanceState)
        splashscreen.setKeepOnScreenCondition { keepSplashScreen }
        lifecycleScope.launch {
            delay(1000)
            keepSplashScreen = false
        }
        setContent {
            PurrytifyTheme {
                val viewModel: LoginViewModel = hiltViewModel()
                LoginScreen(viewModel = viewModel)

            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val loginState by viewModel.loginState.collectAsState()
    var email by rememberSaveable  { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()

            Log.d("TokenCheckService", "Scheduling token check...")
            TokenCheckServiceScheduler.scheduleTokenCheck(context.applicationContext)

            // Move too MainActivity
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)

            // Close the login activity so user can't go back
            if (context is Activity) {
                context.finish()
            }
            viewModel.resetLoginState()
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isPortrait = maxHeight > maxWidth
        if (isPortrait) {
            Image(
                painter = painterResource(id = R.drawable.login_bg),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (maxHeight * 0.5f) - 40.dp),

                )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = (maxHeight * 0.5f) + 32.dp),
                verticalArrangement = Arrangement.Top,

                ) {
                Text(
                    text = "Millions of Songs.",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)

                )
                Text(
                    text = "Only on Purrytify.",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Email",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                EmailTextField(email, onEmailChange = {email = it})
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Password",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                PasswordTextField(password, onPasswordChange = { password = it })
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        // Handle login action
                        viewModel.login(email, password)

                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )

                ) {
                    Text("Login")
                }
                when (loginState) {
                    is LoginState.Loading ->
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
                    is LoginState.Error -> {
                        val message = (loginState as LoginState.Error).message
                        AlertDialog(
                            onDismissRequest = { viewModel.resetLoginState() },
                            title = { Text("Login Failed") },
                            text = { Text(message) },
                            confirmButton = {
                                Button(onClick = { viewModel.resetLoginState() }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                    is LoginState.Success -> {}
                    is LoginState.Idle -> {}
                }

            }
        }
//        else {
//
//        }
    }
}

@Composable
fun PasswordTextField(password: String, onPasswordChange : (String) -> Unit) {
    var passwordHidden by remember { mutableStateOf(true) }
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        singleLine = true,
        placeholder = { Text("Password", color = MaterialTheme.colorScheme.secondary) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier
            .fillMaxWidth(),
        visualTransformation =
        if (passwordHidden) PasswordVisualTransformation()
        else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { passwordHidden = !passwordHidden })
            {
                val visibilityIcon =
                    if (passwordHidden) Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                val description = if (passwordHidden) "Show password" else "Hide password"
                Icon(imageVector = visibilityIcon, contentDescription = description)
            }
        })
}
@Composable
fun EmailTextField(email: String, onEmailChange: (String) -> Unit){
    TextField(
        value = email,
        onValueChange = onEmailChange,
        singleLine = true,
        placeholder = { Text("Email", color = MaterialTheme.colorScheme.secondary) },
        modifier = Modifier
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
@Preview (showBackground = true)
fun LoginScreenPreview(
    @PreviewParameter(LoginPreviewParameterProvider::class) loginState: LoginState
) {
    PurrytifyTheme(darkTheme = true, dynamicColor = false) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.TopCenter)

            )

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (maxHeight * 0.5f) - 40.dp),
                tint = Color.White
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = (maxHeight * 0.5f) + 32.dp),
                verticalArrangement = Arrangement.Top,

            ) {
                Text(
                    text = "Millions of Songs.",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)

                )
                Text(
                    text = "Only on Purrytify.",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Email",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Password",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                PasswordTextField(password, onPasswordChange = { password = it })
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        // Handle login action
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )

                ) {
                    Text("Login")
                }

            }
        }
    }
}
class LoginPreviewParameterProvider: PreviewParameterProvider<LoginState> {
    override val values: Sequence<LoginState> = sequenceOf(
        LoginState.Idle,
        LoginState.Loading,
        LoginState.Success,
        LoginState.Error("Invalid credentials")
    )
}



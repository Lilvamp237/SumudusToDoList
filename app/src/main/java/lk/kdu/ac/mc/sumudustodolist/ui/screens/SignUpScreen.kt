package lk.kdu.ac.mc.sumudustodolist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import lk.kdu.ac.mc.sumudustodolist.ui.navigation.Screen
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.AuthViewModel
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.AuthState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

//Composable function for the Sign Up screen
//Allows new users to create an account using email and password
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    //Email, password, and confirm password input fields for states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    //Get authentication state from the ViewModel
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //React to changes in authentication state
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                // Sign-up successful message and navigate back
                snackbarHostState.showSnackbar("Sign up successful! Please check your email to verify your account.", duration = SnackbarDuration.Long)
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Welcome.route) { inclusive = false }
                }
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Short)
                authViewModel.resetAuthStateToIdle()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Sign Up") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { // Check if passwords match
                    if (password == confirmPassword) {
                        authViewModel.signUp(email, password)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Passwords do not match.", duration = SnackbarDuration.Short)
                        }
                    }
                },
                // Disable button during loading or if fields are empty
                enabled = authState != AuthState.Loading && password.isNotEmpty() && email.isNotEmpty()
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Sign Up")
                }
            }
            TextButton(onClick = { navController.popBackStack() }) { // Go back to Login
                Text("Already have an account? Login")
            }
        }
    }
}
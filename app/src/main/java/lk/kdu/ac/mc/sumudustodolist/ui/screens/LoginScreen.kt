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

//Composable function for the Login screen
//Allows users to log in using their email and password
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // State for email and password input fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    //Get authentication status from the ViewModel
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    //React to changes in authentication state
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                navController.popBackStack()
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
        topBar = { TopAppBar(title = { Text("Login") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(), //Hide password characters
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { authViewModel.login(email, password) },
                enabled = authState != AuthState.Loading
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Login")
                }
            }
            TextButton(onClick = { navController.navigate(Screen.SignUp.route) }) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}
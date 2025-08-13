package lk.kdu.ac.mc.sumudustodolist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

//Composable function for the Profile screen
//Displays user profile information
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    //Get current user from the AuthViewModel
    val currentUser by authViewModel.currentUser.collectAsState()
    //Local state to hold a potentially refreshed FirebaseUser instance
    var refreshedUser by remember(currentUser) { mutableStateOf(currentUser) }

    //refresh the user data when currentUserFromVM changes
    LaunchedEffect(key1 = currentUser) {
        currentUser?.let { user ->
            try {
                refreshedUser = Firebase.auth.currentUser // Directly fetch the current user from Firebase Auth SDK
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "Failed to reload user: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Refreshed User for display
            refreshedUser?.let { user ->
                Text("User Profile", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Email: ${user.email ?: "Not available"}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Email Verified: ${if (user.isEmailVerified) "Yes" else "No (Please check your email)"}")

                if (!user.isEmailVerified) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        user.sendEmailVerification()
                    }) {
                        Text("Resend Verification Email")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { authViewModel.signOut() }) {
                    Text("Sign Out")
                }
            } ?: run {
                Text("Not Logged In", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    navController.navigate(lk.kdu.ac.mc.sumudustodolist.ui.navigation.Screen.Login.route)
                }) {
                    Text("Login / Sign Up")
                }
            }
        }
    }
}
package lk.kdu.ac.mc.sumudustodolist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.BackupRestoreState
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.SettingsViewModel
import androidx.navigation.NavController
import lk.kdu.ac.mc.sumudustodolist.ui.navigation.Screen
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.AuthViewModel
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.AuthState

//Composable function for the Settings screen
//Provides options for data backup and restore, checks if user is logged in
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val backupRestoreState by settingsViewModel.backupRestoreState.collectAsState()
    // Observe the authentication state
    val authS by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(backupRestoreState) {
        when (val state = backupRestoreState) {
            is BackupRestoreState.Success -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Short)
                settingsViewModel.resetBackupRestoreState()
            }
            is BackupRestoreState.Error -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                settingsViewModel.resetBackupRestoreState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Settings & Backup") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (authS) { // UI displayed depends on authentication state
                is AuthState.Authenticated -> {
                    Text("Backup & Restore", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = { settingsViewModel.backupData() },
                        enabled = backupRestoreState == BackupRestoreState.Idle
                    ) { Text("Backup Data to Firebase") }

                    Button(
                        onClick = { settingsViewModel.restoreData() },
                        enabled = backupRestoreState == BackupRestoreState.Idle
                    ) { Text("Restore Data from Firebase") }

                    if (backupRestoreState == BackupRestoreState.InProgress) {
                        CircularProgressIndicator() // Show a progress indicator if an operation is in progress
                    }
                }
                else -> {
                    Text("Please login to use Backup & Restore features.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { navController.navigate(Screen.Login.route) }) {
                        Text("Login / Sign Up")
                    }
                }
            }
        }
    }
}
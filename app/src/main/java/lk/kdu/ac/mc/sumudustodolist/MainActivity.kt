package lk.kdu.ac.mc.sumudustodolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import lk.kdu.ac.mc.sumudustodolist.ui.navigation.AppNavigation
import lk.kdu.ac.mc.sumudustodolist.ui.theme.SumudusTodoListTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import android.content.Intent
import android.os.Build
import android.Manifest
import android.app.AlarmManager

//The main entry point of the application
//This activity hosts the Jetpack Compose UI and handles initial permission requests
class MainActivity : ComponentActivity() {
    // ActivityResultLauncher for requesting runtime permissions.
    // This specifically handles the result of the POST_NOTIFICATIONS permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) { // If notification permission is granted, then proceed to check/request exact alarm permission
                checkAndRequestExactAlarmPermission()
            }
        }

    //Checks if the app can schedule exact alarms
    private fun checkAndRequestExactAlarmPermission() {
        // This check is relevant only for Android S (API 31) and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().apply {
                    action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                }.let {
                    try {
                        startActivity(it)
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Could not open exact alarm settings", e)
                    }
                }
            }
        }
    }

    //Initiates the process of asking for notification permission on Android Tiramisu (API 33) and above
    private fun askNotificationPermission() {
        // Notification permission is a runtime permission only on Android 13 (Tiramisu, API 33) and newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33 an upper
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Notification permission is granted by default before Android 13
            checkAndRequestExactAlarmPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content of the activity using Jetpack Compose
        setContent {
            // Apply the custom theme
            SumudusTodoListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LaunchedEffect(key1 = true) {
                        askNotificationPermission() // Request necessary permissions
                    }
                    // AppNavigation composable sets up the navigation graph for the app
                    AppNavigation()
                }
            }
        }
    }
}
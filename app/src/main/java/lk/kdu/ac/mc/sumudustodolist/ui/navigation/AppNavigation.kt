package lk.kdu.ac.mc.sumudustodolist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import lk.kdu.ac.mc.sumudustodolist.ui.screens.*
import lk.kdu.ac.mc.sumudustodolist.ui.viewmodel.*

//Defines all navigations between app screens
@Composable
fun AppNavigation() {
    val navController = rememberNavController() //Manages navigation state and actions
    val authViewModel: AuthViewModel = viewModel() //Shared AuthViewModel

    //Defines all screens
    NavHost(navController = navController, startDestination = Screen.Welcome.route) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        composable(Screen.ListsOverview.route) {
            ListsOverviewScreen(navController = navController, viewModel = viewModel(), authViewModel = authViewModel)
        }
        composable(
            route = Screen.ListDetail.route,
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) {
            ListDetailScreen(navController = navController, viewModel = viewModel())
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, settingsViewModel = viewModel(), authViewModel = authViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController, authViewModel = authViewModel)
        }
    }
}
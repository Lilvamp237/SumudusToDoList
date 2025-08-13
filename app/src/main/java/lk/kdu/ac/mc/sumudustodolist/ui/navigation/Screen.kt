package lk.kdu.ac.mc.sumudustodolist.ui.navigation

//Represents all screens within the app navigation
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object ListsOverview : Screen("lists_overview")
    object ListDetail : Screen("list_detail/{listId}") {
        fun createRoute(listId: Int) = "list_detail/$listId"
    }
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object Login : Screen("login")
    object SignUp : Screen("signup")
}
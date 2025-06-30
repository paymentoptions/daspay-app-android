import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigator() {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            navController.currentBackStackEntryFlow.collect { backStackEntry ->
            }
        }
    }

    NavHost(navController = navController, startDestination = "authcheckscreen") {
        //composable("authcheckscreen") { AuthCheckScreen(navController) }
        composable("biometricAuthScreen") { BiometricAuthScreen({}, {}, navController, true) }
//        composable("loginScreen") { LoginScreen(navController) }
        composable("drawerScreen") { Drawer(navController) }
    }
}

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paymentoptions.pos.ui.composables.LoginScreen

@Composable
fun Navigator() {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            println("Back stack changed:")
            navController.currentBackStackEntryFlow.collect { backStackEntry ->
                println("Current route: ${backStackEntry.destination.route}")
            }
        }
    }

    NavHost(navController = navController, startDestination = "authCheckScreen") {
        composable("authCheckScreen") { AuthCheckScreen(navController) }
        composable("biometricAuthScreen") { BiometricAuthScreen({}, {}, navController, true) }
        composable("loginScreen") { LoginScreen(navController) }
        composable("drawerScreen") { Drawer(navController) }
    }
}

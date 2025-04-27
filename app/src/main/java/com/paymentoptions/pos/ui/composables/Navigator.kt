import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paymentoptions.pos.ui.composables.PaymentOptionsLoginScreen

@Composable
fun Navigator() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "authCheckScreen") {
        composable("authCheckScreen") { AuthCheckScreen(navController) }
        composable("biometricAuthScreen") { BiometricAuthScreen({}, {}, navController) }
        composable("paymentOptionsLoginScreen") { PaymentOptionsLoginScreen(navController) }
        composable("drawerScreen") { Drawer(navController) }
    }
}

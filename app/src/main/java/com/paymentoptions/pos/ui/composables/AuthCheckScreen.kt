import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences

@Composable
fun AuthCheckScreen(navController: NavController) {
    val context = LocalContext.current
    val authDetails = SharedPreferences.getAuthDetails(context)
    val biometricStatus = !SharedPreferences.getBiometricsStatus(context)
    if (authDetails?.success == true) {
        BiometricAuthScreen(
            {
                navController.navigate("drawerScreen") {
                    popUpTo("loginScreen") { inclusive = true }
                }
            },
            {
                Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show()
            },
            navController,
            biometricStatus
        )
    } else {
        SharedPreferences.clearSharedPreferences(context)
        navController.navigate("loginScreen") {
            popUpTo(0) { inclusive = true }
        }
    }
}

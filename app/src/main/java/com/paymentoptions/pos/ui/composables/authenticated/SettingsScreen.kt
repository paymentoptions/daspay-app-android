import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.services.apiService.SignOutResponse
import com.paymentoptions.pos.services.apiService.endpoints.signOut
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.theme.Orange10
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    var signOutLoader by remember { mutableStateOf(false) }
    var signOutResponse: SignOutResponse? = null
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showBiometricScreen by remember { mutableStateOf(false) }
    var biometricsEnabled by remember { mutableStateOf(SharedPreferences.getBiometricsStatus(context)) }
    val isBiometricsAvailable = isBiometricAvailable(context)

    if (showBiometricScreen)
        BiometricAuthScreen(
            {
            SharedPreferences.saveBiometricsStatus(context, !biometricsEnabled)
            biometricsEnabled = !biometricsEnabled
            showBiometricScreen = false
        }, {
            //SharedPreferences.saveBiometricsStatus(context, false)
            showBiometricScreen = false
        }, navController,
            false
        )
    else
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            if (isBiometricsAvailable)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Biometrics")
                    Switch(
                        checked = biometricsEnabled,
                        onCheckedChange = {
                            try {
                                showBiometricScreen = true
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Unable to set biometrics status",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }

            Button(
                onClick = {
                    scope.launch {
                        signOutLoader = true

                        try {
                            signOutResponse = signOut(context)
                            println("signOutResponse: $signOutResponse")

                            if (signOutResponse == null) {
                                navController.navigate("loginScreen") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }

                            signOutResponse?.let {
                                if (it.success) {
                                    SharedPreferences.clearSharedPreferences(context)
                                    navController.navigate("loginScreen") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        } catch (e: Exception) {
//                            Toast.makeText(context, "Unable to sign out", Toast.LENGTH_LONG).show()

                            SharedPreferences.clearSharedPreferences(context)
                            navController.navigate("loginScreen") {
                                popUpTo(0) { inclusive = true }
                            }

                            println("Error: ${e.toString()}")
                        } finally {
                            signOutLoader = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange10),
                shape = RoundedCornerShape(50),
                enabled = !signOutLoader,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {

                if (signOutLoader)
                    CustomCircularProgressIndicator("Signing out")
                else
                    Text(
                        "Sign Out",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
            }
        }
}
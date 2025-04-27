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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.apiService.SignOutResponse
import com.paymentoptions.pos.apiService.endpoints.signOut
import com.paymentoptions.pos.device.SharedPreferences

@Composable
fun SettingsScreen(navController: NavController): Unit {
    var signOutLoader by remember { mutableStateOf(false) }
    var signOutResponse: SignOutResponse? = null
    val context = LocalContext.current

    var biometricsEnabled by remember { mutableStateOf(SharedPreferences.getBiometricsStatus(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {


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
                        SharedPreferences.saveBiometricsStatus(context, it)
                        biometricsEnabled = it
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
                signOutLoader = true

                try {


                    signOutResponse = signOut(context)
                    println("signOutResponse: $signOutResponse")

                    if (signOutResponse.success) {
                        SharedPreferences.clearSharedPreferences(context)
                        navController.navigate("authCheckScreen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Unable to sign out", Toast.LENGTH_LONG).show()
                    println("Error: ${e.toString()}")

                }

                signOutLoader = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                if (signOutLoader) "Loading..." else "Logout",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.theme.Orange10

@Composable
fun FcmTokenScreen(navController: NavController) {

    val context = LocalContext.current
    var fcmToken by remember { mutableStateOf<String?>("") }
    var loaderState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loaderState = true
        fcmToken = SharedPreferences.getFcmToken(context)
        loaderState = false
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("FCM Token")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, color = Color.Gray),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            SelectionContainer {
                if (loaderState)
                    CustomCircularProgressIndicator("Loading...", Orange10)
                else
                    Text(fcmToken.toString(), Modifier.padding(50.dp))
            }
        }
    }
}
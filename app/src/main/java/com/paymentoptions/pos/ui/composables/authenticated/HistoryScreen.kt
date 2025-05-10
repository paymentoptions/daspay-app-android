import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.endpoints.transactionsList
import com.paymentoptions.pos.ui.composables.TransactionCard
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.theme.Orange10


@Composable
fun HistoryScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactionList by remember { mutableStateOf<TransactionListResponse?>(null) }

    fun exitToLoginScreen() {
        navController.navigate("loginScreen") {
            popUpTo(0) { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        apiResponseAvailable = false
        transactionList = transactionsList(context, 25, 0)
        apiResponseAvailable = true
    }

    if (apiResponseAvailable) {
        if (transactionList == null) {
            Toast.makeText(context, "Token expired. Please log in again.", Toast.LENGTH_LONG).show()
            navController.navigate("loginScreen") {
                popUpTo(0) { inclusive = true }
            }

        }

        transactionList?.let {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {

                for (transaction in it.data.records) {
                    TransactionCard(transaction, ::exitToLoginScreen)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CustomCircularProgressIndicator(null, Orange10)
        }
    }
}
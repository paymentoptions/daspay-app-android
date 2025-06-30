import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.services.apiService.TransactionListResponse
import com.paymentoptions.pos.services.apiService.endpoints.transactionsList
import com.paymentoptions.pos.ui.composables._components.TransactionCard
import com.paymentoptions.pos.ui.composables._components.CustomCircularProgressIndicator
import com.paymentoptions.pos.ui.theme.Orange10
import kotlin.math.ceil


@Composable
fun HistoryScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var apiResponseAvailable by remember { mutableStateOf(false) }
    var transactionList by remember { mutableStateOf<TransactionListResponse?>(null) }
    var filters = mapOf<String, String>(
        "ALL" to "ALL",
        "SUCCESSFUL" to "SUCCESS",
        "REFUND" to "REFUNDED",
        "NOTSUCCESSFUL" to "FAILED"
    )
    var selectedFilterKey by remember { mutableStateOf("ALL") }
    var selectedFilterValue by remember { mutableStateOf("ALL") }
    var take: Int by remember { mutableIntStateOf(30) }
    var currentPage: Int by remember { mutableIntStateOf(1) }
    var maxPage: Int by remember { mutableIntStateOf(1) }
    var transactionsWithTrackId = mutableMapOf<String, Boolean>()

    fun firstPageHandler() {
        currentPage = 1
    }

    fun backPageHandler() {
        currentPage--
    }

    fun nextPageHandler() {
        currentPage++
    }

    fun lastPageHandler() {
        currentPage = maxPage
    }

    fun exitToLoginScreen() {
        navController.navigate("loginScreen") {
            popUpTo(0) { inclusive = true }
        }
    }

    LaunchedEffect(currentPage) {
        apiResponseAvailable = false
        try {
            val skip = (currentPage - 1) * take
            transactionList = transactionsList(context, take, skip)

            if (transactionList == null) {
                maxPage = 0
            } else {
                maxPage =
                    ceil(transactionList!!.data.total_count.toDouble() / take.toDouble()).toInt()
            }
            apiResponseAvailable = true
        } catch (e: Exception) {

        }
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
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = currentPage > 1) {
                                    firstPageHandler()
                                },
                            contentAlignment = Alignment.Center,

                            ) {
                            if (!apiResponseAvailable)
                                CustomCircularProgressIndicator()
                            else
                                Text(
                                    "First",
                                    color = if (currentPage > 1) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = currentPage > 1) {
                                    backPageHandler()
                                },
                            contentAlignment = Alignment.Center,

                            ) {
                            if (!apiResponseAvailable)
                                CustomCircularProgressIndicator()
                            else
                                Text(
                                    "Prev",
                                    color = if (currentPage > 1) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = currentPage < maxPage) {
                                    nextPageHandler()
                                },
                            contentAlignment = Alignment.Center,

                            ) {
                            if (!apiResponseAvailable)
                                CustomCircularProgressIndicator()
                            else
                                Text(
                                    "Next",
                                    color = if (currentPage < maxPage) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = currentPage < maxPage) {
                                    lastPageHandler()
                                },
                            contentAlignment = Alignment.Center,

                            ) {
                            if (!apiResponseAvailable)
                                CustomCircularProgressIndicator()
                            else
                                Text(
                                    "Last",
                                    color = if (currentPage < maxPage) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                        }
                    }

                    CustomDropdown(filters, selectedFilterValue, { key, value ->
                        selectedFilterKey = key
                        selectedFilterValue = value

                    })
                }

                for (transaction in it.data.records) {
                    println("transaction: $transaction")
                    var skip = true

                    if (transaction.trackID !== "N/A")
                        transactionsWithTrackId[transaction.trackID] = true

                    if (!transactionsWithTrackId.contains(transaction.uuid)) {
                        if ((selectedFilterKey === "ALL" || (selectedFilterKey == transaction.status.uppercase() && transaction.TransactionType.uppercase() != "REFUND") || selectedFilterKey == transaction.TransactionType.uppercase()))
                            skip = false
                    }

                    if (!skip)
                        TransactionCard(transaction, ::exitToLoginScreen, {
                        })
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
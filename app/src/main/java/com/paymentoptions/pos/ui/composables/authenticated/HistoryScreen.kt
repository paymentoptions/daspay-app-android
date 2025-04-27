import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.apiService.TransactionListDataRecord
import com.paymentoptions.pos.apiService.endpoints.transactionsList
import java.time.OffsetDateTime
import java.util.Date

@Composable
fun TransactionCard(transaction: TransactionListDataRecord) {

    val dateString = transaction.Date//"2025-04-23T03:38:57.349+00:00"
    val dateTime = OffsetDateTime.parse(dateString)
    val date: Date = Date.from(dateTime.toInstant())

    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp)
    ) {

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)) {

            SuggestionChip(
                onClick = {
                },
                label = {
                    Text(
                        text = transaction.status,
                        fontSize = 8.sp
                    )
                },
                modifier = Modifier
                    .height(20.dp)
                    .align(alignment = Alignment.End),
            )

            Text(
                text = "Transaction ID: " + transaction.uuid,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 20.dp)
            )

            Text(
                text = "Card: ${transaction.CardNumber}",
                fontSize = 10.sp,
            )

            Text(
                text = "Amount: ${transaction.CurrencyCode} ${transaction.amount}",
                fontSize = 10.sp,
            )

            Text(
                text = "Date: $date",
                fontSize = 10.sp,
            )
        }
    }
}


@Composable
fun HistoryScreen(navController: NavController): Unit {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var transactionList by remember { mutableStateOf(transactionsList(context, 25, 0)) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        for (transaction in transactionList.data.records) {
            TransactionCard(transaction)
        }
    }

}
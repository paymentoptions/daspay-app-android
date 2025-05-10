import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paymentoptions.pos.ClientHeadlessImpl
import com.paymentoptions.pos.device.DeveloperOptions
import com.paymentoptions.pos.device.Nfc
import com.paymentoptions.pos.ui.theme.DisabledButtonColor
import com.paymentoptions.pos.ui.theme.Orange10
import com.theminesec.lib.dto.common.Amount
import com.theminesec.lib.dto.poi.PoiRequest
import com.theminesec.lib.dto.transaction.TranType
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.model.WrappedResult
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

@Composable
fun SaleScreen(navController: NavController) {
    var showNFCNotPresent by remember { mutableStateOf(false) }
    var showNFCNotEnabled by remember { mutableStateOf(false) }
    var showDeveloperOptionsEnabled by remember { mutableStateOf(false) }
    var rawInput by remember { mutableStateOf("") }
    var posReferenceId by remember { mutableStateOf("-") }

    fun formatAmount(input: String): String {
        if (input.isEmpty()) return "0.00"
        val cents = input.toLong()
        val dollars = cents / 100
        val centPortion = (cents % 100).toString().padStart(2, '0')
        return "$dollars.$centPortion"
    }

    val formattedAmount = formatAmount(rawInput)
    var description by remember { mutableStateOf("") }
    val onKeyPress: (String) -> Unit = { key ->
        when (key) {
            "←" -> {
                if (rawInput.isNotEmpty()) {
                    rawInput = rawInput.dropLast(1)
                }
            }

            else -> {
                // Avoid too many digits
                if (rawInput.length < 9) {
                    rawInput += key
                }
            }
        }
    }
    val background = Color(0xFF121017)
    val buttonColor = Color(0xFF1E1E1E)
    val buttonBorder = Color(0xFF3A3A3A)
    val textColor = Color(0xFFEFEFEF)
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("00", "0", "←")
    )
    val context = LocalContext.current
    val activity = context as? Activity

    val launcher = rememberLauncherForActivityResult(
        HeadlessActivity.contract(ClientHeadlessImpl::class.java)
    )
    {

        var completedSaleTranId: String? = ""
        var completedSalePosReference: String? = ""
        var completedSaleRequestId: String? = ""

//            viewModel.resetRandomPosReference()
//            viewModel.writeMessage("ActivityResult: $it")
        when (it) {
            is WrappedResult.Success -> {
                if (it.value.tranType == TranType.SALE) {

                    completedSaleTranId = it.value.tranId
                    completedSalePosReference = it.value.posReference
                    completedSaleRequestId = it.value.actions.firstOrNull()?.requestId
                }

                Log.d(
                    "Success ->",
                    "completedSaleTranId: $completedSaleTranId | completedSalePosReference: $completedSalePosReference | completedSaleRequestId: $completedSaleRequestId | it: ${it.value}"
                )

                Toast.makeText(
                    context,
                    "Transaction of $$formattedAmount was successful",
                    Toast.LENGTH_LONG
                ).show()

                rawInput = ""

//                    if (it.value.tranType == TranType.REFUND) {
//                        completedRefundTranId = it.value.tranId
//                    }
            }

            is WrappedResult.Failure -> {
//                    viewModel.writeMessage("Failed")
                Log.d("Failed ->", "Payment Failed")
                Toast.makeText(
                    context,
                    "Transaction of $$formattedAmount was failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Sale", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$$formattedAmount",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFAAB2C8)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Add description", color = Color.Gray) },
                modifier = Modifier
                    .width(160.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Normal,
                    textDecoration = TextDecoration.None
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = background,
                    focusedContainerColor = background,
                    focusedIndicatorColor = Orange10,
                    focusedLabelColor = Orange10,
                    unfocusedLabelColor = Color.DarkGray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorContainerColor = Color.White,
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.4f)
                                .padding(horizontal = 5.dp)
                                .border(1.dp, buttonBorder, RoundedCornerShape(8.dp))
                                .background(buttonColor, RoundedCornerShape(8.dp))
                                .clickable { onKeyPress(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "←") {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            } else {
                                Text(
                                    key,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            CustomDialog(
                showDialog = showDeveloperOptionsEnabled,
                title = "Error",
                text = "You need to disable developer options to proceed further.",
                acceptButtonText = "Exit",
                cancelButtonText = "Developer Options",
                onAccept = {
                    showDeveloperOptionsEnabled = false
                    activity?.finish()
                },
                onDismiss = {
                    showDeveloperOptionsEnabled = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                    context.startActivity(intent)
                    activity?.finish()
                },
            )

            CustomDialog(
                showDialog = showNFCNotPresent,
                title = "NFC Required",
                text = "Your device does not support NFC.",
                acceptButtonText = "Exit",
                cancelButtonText = "Cancel",
                onAccept = {
                    showNFCNotPresent = false
                    activity?.finish()
                },
                onDismiss = { showNFCNotPresent = false },
            )

            CustomDialog(
                showDialog = showNFCNotEnabled,
                title = "NFC Required",
                text = "This feature needs NFC. Please enable it in your device settings.",
                acceptButtonText = "Go to Settings",
                cancelButtonText = "Cancel",
                onAccept = {
                    showNFCNotEnabled = false
                    val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                    context.startActivity(intent)
                },
                onDismiss = { showNFCNotEnabled = false },
            )


            // Charge Button
            val chargeEnabled = rawInput.isNotEmpty() && rawInput.toLong() > 0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (chargeEnabled) Orange10 else DisabledButtonColor)
                    .clickable(enabled = chargeEnabled) {

                        if (DeveloperOptions.isEnabled(context)) {
                            showDeveloperOptionsEnabled = true
                        } else {
                            var nfcStatusPair = Nfc.getStatus(context)

                            if (!nfcStatusPair.first) {
                                showNFCNotPresent = true
                            } else if (!nfcStatusPair.second) {
                                showNFCNotEnabled = true
                            } else {
                                val uuid: UUID = UUID.randomUUID()
                                posReferenceId = uuid.toString()

                                launcher.launch(
                                    PoiRequest.ActionNew(
                                        tranType = TranType.SALE,
                                        amount = Amount(
                                            BigDecimal(formattedAmount),
                                            Currency.getInstance("USD"),
                                        ),
                                        profileId = "prof_01HYYPGVE7VB901M40SVPHTQ0V",
                                        posReference = posReferenceId
                                    )
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.Center,

                ) {
                Text(
                    "Charge",
                    color = if (chargeEnabled) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            "©2025, Payment Options Limited", color = Color.Gray, fontSize = 12.sp
        )
    }
}
package com.paymentoptions.pos.ui.composables.screens._flow.refundflow.refundTransaction

import android.os.Handler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.composables.screens.status.MessageForStatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreen
import com.paymentoptions.pos.ui.composables.screens.status.StatusScreenType

@Composable
fun RefundTransactionScreen(navController: NavController) {
    val enableScrollingInsideBottomSectionContent = false
    var refundStatus by remember { mutableStateOf<StatusScreenType?>(null) }
    LocalContext.current

    fun updateRefundStatus(newRefundStatus: StatusScreenType?) {
        refundStatus = newRefundStatus
    }

    when (refundStatus) {
        null -> {
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.TOGGLE_BUTTON,
                bottomSectionPaddingInDp = 0.dp,
                imageBelowLogo = { },
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent
            ) {
                BottomSectionContent(
                    navController,
                    enableScrolling = enableScrollingInsideBottomSectionContent,
                    updateRefundStatus = { updateRefundStatus(it) })
            }
        }

        StatusScreenType.PROCESSING -> {
            val dataMessage = MessageForStatusScreen(
                text = "Processing...", statusScreenType = StatusScreenType.PROCESSING
            )
            StatusScreen(navController, dataMessage, strategyFn = {
//                Handler().postDelayed({
//                    updateRefundStatus(StatusScreenType.SUCCESS)
//                }, 2000)
            })
        }

        StatusScreenType.SUCCESS -> {
            val dataMessage = MessageForStatusScreen(
                text = "Refund Submitted", statusScreenType = StatusScreenType.SUCCESS
            )
            StatusScreen(navController, dataMessage, strategyFn = {
//                Handler().postDelayed({
////                    updateRefundStatus(StatusScreenType.ERROR)
//                    navController.navigate(Screens.RefundInitiated.route)
//                }, 2000)
            })
        }

        StatusScreenType.ERROR -> {
            val dataMessage = MessageForStatusScreen(
                text = "Refund Failed", statusScreenType = StatusScreenType.ERROR
            )
            StatusScreen(navController, dataMessage, strategyFn = {
                Handler().postDelayed({
//                    updateRefundStatus(null)
//
//                    Toast.makeText(
//                        context,
//                        "Error processing refund. Try again..",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }, 2000)
            })
        }
    }


}
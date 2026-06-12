package com.paymentoptions.pos.ui.composables.screens.transactiondetails

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.receipt.ReceiptBottomSectionContent
import java.util.Date

enum class TransactionDetailsScreenType {
    DETAILS_SCREEN, RECEIPT_SCREEN
}

@Composable
fun TransactionDetailsScreen(
    navController: NavController,
    transaction: TransactionListDataRecord? = null,
) {
    val enableScrollingInsideBottomSectionContent = true

    var transactionDetailsScreenType by remember {
        mutableStateOf(TransactionDetailsScreenType.DETAILS_SCREEN)
    }

    when (transactionDetailsScreenType) {
        TransactionDetailsScreenType.RECEIPT_SCREEN ->
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMinHeightRatio = 0.75f,
                bottomSectionMaxHeightRatio = 0.75f,
                enableScrollingOfBottomSectionContent = false,
                enableZigZagContainerForBottomSection = true,
                imageBelowLogo = {
                    Text(
                        text = "Receipt",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }) {
                ReceiptBottomSectionContent(
                    navController,
                    enableScrolling = true,
                    transactionId = transaction?.uuid ?:"",
                    signatureBitmap = null,
                    signatureDate = Date(),
                )
            }

        TransactionDetailsScreenType.DETAILS_SCREEN ->
            SectionedLayout(
                navController = navController,
                bottomSectionMinHeightRatio = 0.9f,
                bottomSectionMaxHeightRatio = 0.9f,
                bottomSectionPaddingInDp = 0.dp,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent,
                blurTopSection = true
            ) {
                transaction?.let {
                    TransactionBottomSectionContent(
                        navController = navController,
                        transaction = it,
                        updateDetailsScreenType = { type ->
                            transactionDetailsScreenType = type
                        }
                    )
                }
            }
    }
}


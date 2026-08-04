package com.paymentoptions.pos.ui.composables.screens.transactiondetails


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.paymentoptions.pos.ui.composables._components.ShowReceiptView
import com.paymentoptions.pos.ui.composables.layout.sectioned.BottomBarContent
import com.paymentoptions.pos.ui.composables.layout.sectioned.SectionedLayout
import com.paymentoptions.pos.ui.composables.screens._flow.receiveMoneyFlow.receipt.ReceiptBottomSectionContent
import java.util.Date


@Composable
fun TransactionStatusScreen(
    navController: NavController,
    transactionUUid: String,
    title : String,
    amount : String,
    dateString : String,
    referenceId : String,
    aggregator: String
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
                bottomSectionMinHeightRatio = 0.45f,
                bottomSectionMaxHeightRatio = 0.45f,
                enableScrollingOfBottomSectionContent = false,
                enableZigZagContainerForBottomSection = true,
                imageBelowLogo = {
                    ShowReceiptView()
                }) {
                ReceiptBottomSectionContent(
                    navController,
                    enableScrolling = true,
                    transactionId = transactionUUid ?:"",
                    signatureBitmap = null,
                    signatureDate = Date(),
                )
            }

        TransactionDetailsScreenType.DETAILS_SCREEN ->
            SectionedLayout(
                navController = navController,
                bottomBarContent = BottomBarContent.NAVIGATION_BAR,
                bottomSectionPaddingInDp = 0.dp,
                bottomSectionMinHeightRatio = 0.55f,
                bottomSectionMaxHeightRatio = 0.55f,
                enableScrollingOfBottomSectionContent = !enableScrollingInsideBottomSectionContent,
                ) {
                StatusBottomSectionContent(
                    navController,
                    transactionId = transactionUUid,
                    enableScrolling = true,
                    title = title,
                    amount = amount,
                    dateString = dateString,
                    referenceId = referenceId,
                    aggregator = aggregator,
                    updateDetailsScreenType = { transactionDetailsScreenType = it }
                )
            }
    }

}


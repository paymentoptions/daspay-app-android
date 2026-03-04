package com.paymentoptions.pos.utils

import androidx.compose.ui.graphics.Color
import com.paymentoptions.pos.R
import com.paymentoptions.pos.services.apiService.TransactionListDataRecord

// Status Colors
object TransactionColors {
    val Green = Color(0xFF22C55E)    // Successful & Settled
    val Yellow = Color(0xFFFBBF24)   // Unsettled Sale (Pending)
    val Red = Color(0xFFD52121)      // Unsuccessful / Failed
    val Grey = Color(0xFF9CA3AF)     // Voided
    val Orange = Color(0xFFFC8D3E)   // Refund
}

// Action types for transactions
enum class TransactionAction {
    REFUND,
    VOID,
    NONE
}


/**
 * Determines the status color based on transaction status, settle status, and transaction type
 */
fun getStatusColor(transaction: TransactionListDataRecord): Color {
    val status = transaction.status.uppercase()
    val settleStatus = transaction.SettleStatus?.uppercase() ?: ""
    val transactionType = transaction.TransactionType.uppercase()
    val productType = transaction.ProductType?.uppercase() ?: ""

    return when {
        // Voided transaction (Grey)
        transactionType == "VOIDAUTHORISATION" && productType == "SOFTPOS" -> TransactionColors.Grey

        // Refund transaction (Orange)
        transactionType == "REFUND" -> TransactionColors.Orange

        // Unsuccessful transaction (Red)
        status == "NOTSUCCESSFUL" || settleStatus == "FAILED" -> TransactionColors.Red

        // Unsettled Sale - Pending (Yellow)
        settleStatus == "PENDING" && productType == "SOFTPOS" -> TransactionColors.Yellow
        status == "PENDING" && productType == "QR" -> TransactionColors.Yellow
        status == "PENDING" && productType == "PBL" -> TransactionColors.Yellow

        // Successful & Settled (Green)
        status == "SUCCESSFUL" && settleStatus == "SETTLED" -> TransactionColors.Green

        // Default successful (Green)
        status == "SUCCESSFUL" -> TransactionColors.Green

        // Default fallback (Red)
        else -> TransactionColors.Red
    }
}

/**
 * Determines the financial direction sign for the amount
 * Returns: "+" for money received, "-" for money returned, "" for unsuccessful
 */
fun getAmountSign(transaction: TransactionListDataRecord): String {
    val status = transaction.status.uppercase()
    val settleStatus = transaction.SettleStatus?.uppercase() ?: ""
    val transactionType = transaction.TransactionType.uppercase()

    return when {
        // Not Successful - No sign
        status == "NOTSUCCESSFUL" || settleStatus == "FAILED" -> ""

        // Voided transactions - Negative
        transactionType == "VOID" || transactionType == "VOIDAUTHORISATION" -> "-"

        // Refund transactions
        transactionType == "REFUND" -> "-"

        // Money received (Sale) - Positive
        status == "SUCCESSFUL" && (transactionType == "PURCHASE" || transactionType == "AUTHORISATION") -> "+"

        // Default - No sign
        else -> ""
    }
}

/**
 * Gets the appropriate icon resource based on product type and transaction type
 */
fun getTransactionIcon(transaction: TransactionListDataRecord): Int {
    val transactionType = transaction.TransactionType.uppercase()
    val productType = transaction.ProductType?.uppercase() ?: ""

    return when {
        // Refund icon for refund transactions
        transactionType == "REFUND" -> R.drawable.refund_icon

        transactionType == "VOIDAUTHORISATION" -> R.drawable.void_icon

        // SOFTPOS (Card payment)
        productType == "SOFTPOS" -> R.drawable.icon_card

        // QR payment
        productType == "QR" -> R.drawable.icon_qr

        // PBL (Payment By Link)
        productType == "PBL" -> R.drawable.icon_link

        // Card payment fallback
        transaction.PaymentType == "CARDPAYMENT" -> R.drawable.icon_card

        // Default money icon
        else -> R.drawable.icon_money
    }
}

/**
 * Determines which action button to show (REFUND, VOID, or NONE)
 */
fun getAvailableAction(transaction: TransactionListDataRecord): TransactionAction {
    if(transaction.IsVoided == true) return TransactionAction.NONE
    if(transaction.IsRefunded == true) return TransactionAction.NONE

    val transactionType = transaction.TransactionType.uppercase()
    val settleStatus = transaction.SettleStatus?.uppercase() ?: ""
    val productType = transaction.ProductType?.uppercase() ?: ""
    val status = transaction.status.uppercase()

    // Only successful transactions can have actions
    if (status != "SUCCESSFUL") return TransactionAction.NONE

    return when {
        // VOID: Unsettled SOFTPOS AUTHORISATION
        transactionType == "AUTHORISATION" && settleStatus == "PENDING" && productType == "SOFTPOS" -> TransactionAction.VOID

        // VOID: Unsettled SOFTPOS AUTHORISATION
        transactionType == "AUTHORISATION" && settleStatus == "PENDING" && productType == "QR" -> TransactionAction.VOID

        // VOID: Unsettled SOFTPOS AUTHORISATION
        transactionType == "AUTHORISATION" && settleStatus == "PENDING" && productType == "PBL" -> TransactionAction.VOID

        // REFUND: Settled SOFTPOS AUTHORISATION
        transactionType == "AUTHORISATION" && settleStatus == "SETTLED" && productType == "SOFTPOS" -> TransactionAction.REFUND

        transactionType == "PURCHASE" && settleStatus == "SETTLED" && productType == "SOFTPOS" -> TransactionAction.REFUND

        // REFUND: PBL PURCHASE
        transactionType == "PURCHASE" && productType == "PBL" -> TransactionAction.REFUND

        // REFUND: QR PURCHASE
        transactionType == "PURCHASE" && productType == "QR" -> TransactionAction.REFUND

        // No action available
        else -> TransactionAction.NONE
    }
}

/**
 * Gets a human-readable transaction type label
 */
fun getTransactionTypeLabel(transaction: TransactionListDataRecord): String {
    val transactionType = transaction.TransactionType.uppercase()

    return when {
        transactionType == "REFUND" -> "Refund"
        transactionType == "VOIDAUTHORISATION" -> "Voided"
        transactionType == "AUTHORISATION" || transactionType == "PURCHASE" -> "Sale"
        else -> transactionType.lowercase().replaceFirstChar { it.uppercase() }
    }
}
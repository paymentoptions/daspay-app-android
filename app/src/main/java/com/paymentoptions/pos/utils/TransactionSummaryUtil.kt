package com.paymentoptions.pos.utils

import androidx.compose.ui.graphics.Color
import com.paymentoptions.pos.R
import com.paymentoptions.pos.logger.AppLogger
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

    val result =  when {
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

    AppLogger.debug("getAmountSign", "status: $status, settleStatus: $settleStatus, transactionType: $transactionType, result: $result")

    return result
}

fun getTransactionAmount(transaction: TransactionListDataRecord): Float {
    val status = transaction.status.uppercase()
    val settleStatus = transaction.SettleStatus?.uppercase() ?: ""
    val transactionType = transaction.TransactionType.uppercase()

    when {
        // Not Successful - No sign
        status == "NOTSUCCESSFUL" || settleStatus == "FAILED" -> {return 0f}
        // Voided transactions - Negative
        transactionType == "VOID" || transactionType == "VOIDAUTHORISATION" -> {
            return  -transaction.amount.toFloat()
        }
        // Refund transactions
        transactionType == "REFUND" -> {
            return -transaction.amount.toFloat()
        }
        // Money received (Sale) - Positive
        status == "SUCCESSFUL" && (transactionType == "PURCHASE" || transactionType == "AUTHORISATION") -> {
            return +transaction.amount.toFloat()
        }
    }
    return 0f
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
 *
 * ProductType	TransactionType	Payment Status	SettleStatus	Action
 * SOFTPOS	    AUTHORISATION	SUCCESSFUL	    PENDING	        VOID
 * SOFTPOS	    PURCHASE	    SUCCESSFUL	    SETTLED	        REFUND
 * QR	        PURCHASE	    PENDING	        n/a	            n/a
 * QR	        PURCHASE	    SUCCESSFUL	    n/a	            REFUND
 * PBL	        PURCHASE	    SUCCESSFUL	    n/a	            REFUND
 */
fun getAvailableAction(transaction: TransactionListDataRecord): TransactionAction {
    // Already voided or refunded - no action available
    if(transaction.IsVoided == true) return TransactionAction.NONE
    if(transaction.IsRefunded == true) return TransactionAction.NONE

    val TransactionType = transaction.TransactionType.uppercase()
    val SettleStatus = transaction.SettleStatus?.uppercase() ?: ""
    val ProductType = transaction.ProductType?.uppercase() ?: ""
    val status = transaction.status.uppercase()

    return when {
        // SOFTPOS + AUTHORISATION + SUCCESSFUL + PENDING → VOID
        ProductType == "SOFTPOS" &&
        TransactionType == "AUTHORISATION" &&
        status == "SUCCESSFUL" &&
        SettleStatus == "PENDING" -> TransactionAction.VOID

        // SOFTPOS + PURCHASE + SUCCESSFUL + SETTLED → REFUND
        ProductType == "SOFTPOS" &&
        TransactionType == "PURCHASE" &&
        status == "SUCCESSFUL" &&
        SettleStatus == "SETTLED" -> TransactionAction.REFUND

        // QR + PURCHASE + PENDING → NONE
        ProductType == "QR" &&
        TransactionType == "PURCHASE" &&
        status == "PENDING" -> TransactionAction.NONE

        // QR + PURCHASE + SUCCESSFUL → REFUND
        ProductType == "QR" &&
        TransactionType == "PURCHASE" &&
        status == "SUCCESSFUL" -> TransactionAction.REFUND

        // PBL + PURCHASE + SUCCESSFUL → REFUND
        ProductType == "PBL" &&
        TransactionType == "PURCHASE" &&
        status == "SUCCESSFUL" -> TransactionAction.REFUND

        // No action available for any other combination
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

fun shouldShowFullReceipt(transaction: TransactionListDataRecord): Boolean {
    val transactionType = transaction.TransactionType.uppercase()
    val status = transaction.status.uppercase()

    return when {
        // Unsettled Sale - Pending (Yellow)
        transactionType == "REFUND"  -> false
        transactionType == "VOIDAUTHORISATION" -> false
        status == "NOTSUCCESSFUL" -> false

        else -> true
    }

}

fun getStatusText(transaction: TransactionListDataRecord): String {
    val transactionType = transaction.TransactionType.uppercase()
    val status = transaction.status.uppercase()
    val settleStatus = transaction.SettleStatus?.uppercase() ?: ""
    val productType = transaction.ProductType?.uppercase() ?: ""

    return when {
        transactionType == "REFUND" -> "Transaction Refunded"
        transactionType == "VOID" -> "Transaction Voided"
        transactionType == "VOIDAUTHORISATION" -> "Transaction Voided"
        status == "SUCCESSFUL" -> "Transaction Successful"
        // Unsettled Sale - Pending (Yellow)
        settleStatus == "PENDING" && productType == "SOFTPOS" -> "Transaction Pending"
        status == "PENDING" && productType == "QR" -> "Transaction Pending"
        status == "PENDING" && productType == "PBL" -> "Transaction Pending"
        else -> "Transaction Failed"
    }
}

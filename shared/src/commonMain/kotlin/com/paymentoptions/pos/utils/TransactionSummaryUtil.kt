package com.paymentoptions.pos.utils

import androidx.compose.ui.graphics.Color
import com.paymentoptions.pos.network.TransactionListDataRecord
import paymentoptionspos.shared.generated.resources.Res
import paymentoptionspos.shared.generated.resources.refund_icon
import paymentoptionspos.shared.generated.resources.void_icon
import paymentoptionspos.shared.generated.resources.icon_card
import paymentoptionspos.shared.generated.resources.icon_qr
import paymentoptionspos.shared.generated.resources.icon_link
import paymentoptionspos.shared.generated.resources.icon_money
import org.jetbrains.compose.resources.DrawableResource

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
fun getTransactionIcon(transaction: TransactionListDataRecord): DrawableResource {
    val transactionType = transaction.TransactionType.uppercase()
    val productType = transaction.ProductType?.uppercase() ?: ""

    return when {
        // Refund icon for refund transactions
        transactionType == "REFUND" -> Res.drawable.refund_icon

        transactionType == "VOIDAUTHORISATION" -> Res.drawable.void_icon

        // SOFTPOS (Card payment)
        productType == "SOFTPOS" -> Res.drawable.icon_card

        // QR payment
        productType == "QR" -> Res.drawable.icon_qr

        // PBL (Payment By Link)
        productType == "PBL" -> Res.drawable.icon_link

        // Card payment fallback
        transaction.PaymentType == "CARDPAYMENT" -> Res.drawable.icon_card

        // Default money icon
        else -> Res.drawable.icon_money
    }
}

/**
 * Determines which action button to show (REFUND, VOID, or NONE)
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
        else -> transactionType.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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

package com.paymentoptions.pos

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.theminesec.lib.dto.common.Amount
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.ui.AmountView
import com.theminesec.sdk.headless.ui.UiProvider

class ClientHeadlessImpl : HeadlessActivity(){
    override val experimentalScreenProvider = false
    override fun provideUi(): UiProvider {return CustomUiProvider()}
}


class CustomUiProvider(
) : UiProvider(
    amountView = CustomAmountView,
) {
    object CustomAmountView : AmountView {
        override fun createAmountView(
            context: Context,
            amount: Amount,
            description: String?
        ): View {
            return TextView(context).apply {
                val text = "Total Amount\nHK\$ ${amount.value}"
                val spannable = SpannableString(text)

                // "Total Amount" → Gray, 20sp
                spannable.setSpan(
                    ForegroundColorSpan(Color.Gray.toArgb()),
                    0,
                    "Total Amount".length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    AbsoluteSizeSpan(20, true),
                    0,
                    "Total Amount".length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // "HK $ value" → Black, 34sp
                spannable.setSpan(
                    ForegroundColorSpan(Color.Black.toArgb()),
                    "Total Amount\n".length,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    AbsoluteSizeSpan(34, true),
                    "Total Amount\n".length,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                setText(spannable)
            }

        }
    }


}

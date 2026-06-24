package com.paymentoptions.pos

import android.content.Context
import android.content.res.Configuration
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.theminesec.lib.dto.common.Amount
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.ui.AmountView
import com.theminesec.sdk.headless.ui.UiProvider

class ClientHeadlessImpl : HeadlessActivity(){
    override val experimentalScreenProvider = false
    override fun provideUi(): UiProvider {
        AppLogger.debug("TapToPay HeadlessActivity provideUi called; experimentalScreenProvider=$experimentalScreenProvider")
        return CustomUiProvider()
    }
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
            AppLogger.debug("TapToPay amount view creation: amount=${amount.value}, currency=${DPSharedPreferences.getTransactionCurrency(context)}, description=$description")
            return TextView(context).apply {
                val text = "Total Amount\n${DPSharedPreferences.getTransactionCurrency(context).replace("D", "$")} ${amount.value}"
                val spannable = SpannableString(text)


                // Determine color based on theme
                val isDarkTheme = (context.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                val labelColor = if (isDarkTheme) Color.White else Color.Black
                // "Total Amount" → Gray (light) or White (dark), 20sp
                spannable.setSpan(
                    ForegroundColorSpan(labelColor.toArgb()),
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
                    ForegroundColorSpan(labelColor.toArgb()),
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

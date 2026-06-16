package com.paymentoptions.pos

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.paymentoptions.pos.device.DPStorageManager
import com.paymentoptions.pos.logger.AppLogger
import com.theminesec.lib.dto.common.Amount
import com.theminesec.sdk.headless.HeadlessActivity
import com.theminesec.sdk.headless.ui.AmountView
import com.theminesec.sdk.headless.ui.AwaitCardIndicatorView
import com.theminesec.sdk.headless.ui.UiProvider
import com.theminesec.sdk.headless.ui.UiState
import com.theminesec.sdk.headless.ui.UiStateDisplayView

class ClientHeadlessImpl : HeadlessActivity(){
    override val experimentalScreenProvider = false
    override fun provideUi(): UiProvider {return CustomUiProvider()}
}


class CustomUiProvider(
) : UiProvider(
    amountView = CustomAmountView,
    uiStateDisplayView = CustomUiStateDisplayView(),
    awaitCardIndicatorView = CustomAwaitCardIndicatorView
) {
    object CustomAmountView : AmountView {
        override fun createAmountView(
            context: Context,
            amount: Amount,
            description: String?
        ): View {
            AppLogger.debug("TapToPay amount view creation: amount=${amount.value}, currency=${DPStorageManager.getTransactionCurrency()}, description=$description")
            return TextView(context).apply {
                val text = "Total Amount\n${DPStorageManager.getTransactionCurrency()
                    .replace("D", "$")} ${amount.value}"
                val spannable = SpannableString(text)


                // Determine color based on theme
                val isDarkTheme = (context.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                val labelColor = if (isDarkTheme) Color.White else Color.Black
                // "Total Amount" → Gray (light) or White (dark), 14sp
                spannable.setSpan(
                    ForegroundColorSpan(labelColor.toArgb()),
                    0,
                    "Total Amount".length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    AbsoluteSizeSpan(14, true),
                    0,
                    "Total Amount".length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // "HK $ value" → Black, Bold, 34sp
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
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    "Total Amount\n".length,
                    text.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                setText(spannable)
            }

        }
    }

    class CustomUiStateDisplayView : UiStateDisplayView {
        private var titleTextView: TextView? = null
        private var descTextView: TextView? = null
        private var timerTextView: TextView? = null

        override fun createUiStateDisplayView(context: Context, uiState: UiState): View {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 20, 0, 20)
            }

            titleTextView = TextView(context).apply {
                textSize = 20f
                setTypeface(null, Typeface.BOLD)
                setTextColor(AndroidColor.WHITE)
                text = "Tap to Pay"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            descTextView = TextView(context).apply {
                textSize = 13f
                setTextColor(AndroidColor.WHITE)
                text = "Tap and hold card to the back of device"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 4
                }
            }

            timerTextView = TextView(context).apply {
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setTextColor(AndroidColor.parseColor("#44A101")) // green200
                text = ""
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                }
            }

            layout.addView(titleTextView)
            layout.addView(descTextView)
            layout.addView(timerTextView)

            return layout
        }

        override fun onCountdownUpdate(countdownSec: Int) {
            timerTextView?.text = "${countdownSec}s"
            timerTextView?.visibility = if (countdownSec > 0) View.VISIBLE else View.GONE
        }

        override fun onUiStateUpdate(context: Context, uiState: UiState, countdownSec: Int) {
            onCountdownUpdate(countdownSec)
        }
    }

    object CustomAwaitCardIndicatorView : AwaitCardIndicatorView {
        override fun createAwaitCardIndicatorView(context: Context): View {
            return ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
        }
    }
}

package com.paymentoptions.pos

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.paymentoptions.pos.device.NetworkStatusComposable
import com.paymentoptions.pos.ui.theme.PaymentOptionsPOSTheme
import com.theminesec.sdk.headless.HeadlessSetup
import kotlinx.coroutines.launch


class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setup()

        setContent {
            PaymentOptionsPOSTheme {
                NetworkStatusComposable()
            }
        }
    }

    fun setup() = lifecycleScope.launch {
        HeadlessSetup.initialSetup(this@MainActivity)
    }
}

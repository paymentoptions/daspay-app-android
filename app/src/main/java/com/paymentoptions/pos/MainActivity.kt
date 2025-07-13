package com.paymentoptions.pos

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.paymentoptions.pos.device.LockScreenOrientation
import com.paymentoptions.pos.device.NetworkStatusComposable
import com.paymentoptions.pos.ui.theme.AppTheme
import com.paymentoptions.pos.ui.theme.SystemUiController
import com.theminesec.sdk.headless.HeadlessSetup
import kotlinx.coroutines.launch


class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setup()

        setContent {
            SystemUiController()

//            val context = LocalContext.current
//            val biometricStatus = SharedPreferences.getImmersiveModeStatus(context)
//            SetImmersiveMode(biometricStatus)

            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

            AppTheme {
                NetworkStatusComposable()
            }
        }
    }

    fun setup() = lifecycleScope.launch {

        HeadlessSetup.initialSetup(this@MainActivity) {
            withTestCapk = true

        }
        HeadlessSetup.getEmvParams()
        HeadlessSetup.getCapks()
        HeadlessSetup.getTermParam()

    }
}

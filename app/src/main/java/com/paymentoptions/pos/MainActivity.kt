package com.paymentoptions.pos

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.paymentoptions.pos.device.NetworkStatusComposable
import com.paymentoptions.pos.ui.theme.AppTheme
import com.theminesec.sdk.headless.HeadlessSetup
import kotlinx.coroutines.launch


class MainActivity : FragmentActivity() {

    private fun immersiveMode() {
        // Let app draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Get controller to manage system bars
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Hide status and navigation bars for immersive mode
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setup()

        immersiveMode()
        setContent {
            AppTheme {
                NetworkStatusComposable()
            }
        }
    }

    fun setup() = lifecycleScope.launch {
        HeadlessSetup.initialSetup(this@MainActivity)
    }
}

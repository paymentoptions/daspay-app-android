package com.paymentoptions.pos

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.paymentoptions.pos.device.LockScreenOrientation
import com.paymentoptions.pos.device.NetworkStatusComposable
import com.paymentoptions.pos.storage.initAppStorage
import com.paymentoptions.pos.ui.theme.AppTheme
import com.theminesec.sdk.headless.HeadlessSetup
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // ── 1. Initialise platform references ─────────────────────────────
        currentActivity = this

        // ── 2. Initialise encrypted storage ───────────────────────────────
        initAppStorage(applicationContext)

        // ── 3. Token auto-refresher is handled inside shared TokenRepository
        //       Nothing extra needed here.

        // ── 4. Immersive mode ─────────────────────────────────────────────
        immersiveMode()

        // ── 5. Set Compose content using the shared App composable ────────
        setContent {

            AppTheme {
                NetworkStatusComposable()
            }
            //App(buildTimeBaseUrl = BuildConfig.CONFIG_BASE_URL)
        }
    }

    // ── MineSec Headless SDK setup (Android-only) ──────────────────────────
    fun setupHeadless() = lifecycleScope.launch {
        HeadlessSetup.initialSetup(this@MainActivity)
        HeadlessSetup.getEmvParams()
        HeadlessSetup.getCapks()
        HeadlessSetup.getTermParam()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentActivity = null
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun immersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window.statusBarColor     = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

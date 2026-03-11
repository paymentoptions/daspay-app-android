package com.paymentoptions.pos

import android.app.Application
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.logger.Config
import com.paymentoptions.pos.logger.LogConfig
import com.theminesec.sdk.headless.HeadlessSetup
import com.theminesec.sdk.headless.model.WrappedResult
import com.theminesec.sdk.headless.model.setup.SdkInitResp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File


class ClientApp : Application() {
    private val appScope = CoroutineScope(Dispatchers.Main)
    private val _sdkInitStatus = MutableSharedFlow<WrappedResult<SdkInitResp>>(replay = 1)
//    val sdkInitStatus: SharedFlow<WrappedResult<SdkInitResp>> = _sdkInitStatus

    override fun onCreate() {
        super.onCreate()

        initAppLogger()

        // Log the current environment
        AppLogger.info("App started - Build variant: ${BuildConfig.BUILD_TYPE}, Flavor: ${BuildConfig.FLAVOR}")

        appScope.launch {
            val clientAppInitRes =
                HeadlessSetup.initSoftPos(this@ClientApp, "payment-options.license")
            AppLogger.debug("Application init: $clientAppInitRes")
            val res = HeadlessSetup.initialSetup(this@ClientApp)
            AppLogger.debug("Inital Setup---> ${res}")
            _sdkInitStatus.emit(clientAppInitRes)
        }

    }

    private fun initAppLogger() {
        AppLogger.init(
            context = this.applicationContext,
            config = getConfig(),
            appVersion = "Version 2.0",
            onThrowError = {
                // A callback if we need to integrate Sentry or other error platforms
            },
        )
    }

    private fun getConfig(): Config {
        val logFolderPath = "${cacheDir.absolutePath}${File.separator}daspay"
        val logConfig = LogConfig("", logFolderPath)
        return Config(logConfig = logConfig)
    }


}
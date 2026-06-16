package com.paymentoptions.pos

import android.app.Application
import android.widget.Toast
import com.datadog.android.Datadog
import com.datadog.android.DatadogSite
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.Rum
import com.datadog.android.rum.RumConfiguration
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy
import com.paymentoptions.pos.analytics.AnalyticsHelper
import com.paymentoptions.pos.analytics.DatadogAnalyticsDelegate
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
        initDatadogRum()
        setIsDebugBuild(BuildConfig.DEBUG)
        DatadogAnalyticsDelegate.environment = BuildConfig.ENVIRONMENT
        DatadogAnalyticsDelegate.variant = "${BuildConfig.FLAVOR}-${BuildConfig.BUILD_TYPE}"
        AnalyticsHelper.delegate = DatadogAnalyticsDelegate
        AnalyticsHelper.trackAppLaunch()

        // Log the current environment
        AppLogger.info("App started - Build variant: ${BuildConfig.BUILD_TYPE}, Flavor: ${BuildConfig.FLAVOR}")

        appScope.launch {
            try {
                AppLogger.info("MineSec bootstrap started")
                val hasLicenseAsset = try {
                    assets.list("")?.contains("payment-options.license") == true
                } catch (e: Exception) {
                    AppLogger.error(
                        "MineSec bootstrap: failed while checking license asset presence: ${e.message}",
                        e
                    )
                    false
                }
                AppLogger.debug("MineSec bootstrap context: package=${packageName}, process=${android.os.Process.myPid()}, flavor=${BuildConfig.FLAVOR}, buildType=${BuildConfig.BUILD_TYPE}, licenseAssetPresent=$hasLicenseAsset")

                val clientAppInitRes =
                    HeadlessSetup.initSoftPos(this@ClientApp, "payment-options.license")
                AppLogger.debug("Application init: $clientAppInitRes")
                when (clientAppInitRes) {
                    is WrappedResult.Success -> {
                        AppLogger.info("MineSec initSoftPos success: ${clientAppInitRes.value}")
                    }

                    is WrappedResult.Failure -> {
                        AppLogger.error(
                            "MineSec initSoftPos failed: code=${clientAppInitRes.code}, message=${clientAppInitRes.message}, contextual=${clientAppInitRes.contextual}, extra=${clientAppInitRes.extra}"
                        )
                        Toast.makeText(this@ClientApp, "MineSec SDK initialization failed. Please report.", Toast.LENGTH_LONG).show()
                        if (clientAppInitRes.code == 987142) {
                            AppLogger.warn("MineSec initSoftPos failure code 987142 indicates SDK is not initialized; verify AAR compatibility and license loading")
                        }
                    }
                }
                AppLogger.debug("MineSec initialSetup started")
                val res = HeadlessSetup.initialSetup(this@ClientApp)
                AppLogger.debug("MineSec initialSetup response: $res")
                val setupFailureDetected = res.toString().contains("Failure(")
                if (setupFailureDetected) {
                    Toast.makeText(this@ClientApp, "MineSec initial setup reported failures. Please report.", Toast.LENGTH_LONG).show()
                } else {
                    AppLogger.info("MineSec initialSetup completed without reported failures")
                }
                // AppAnalytics.mineSecSdkInitialization(step = "initial_setup", result = "success")
                _sdkInitStatus.emit(clientAppInitRes)
            } catch (e: Exception) {
                AppLogger.error("MineSec bootstrap exception: ${e.message}", e)
            }
        }

    }

    private fun initAppLogger() {
        AppLogger.init(
            context = this.applicationContext,
            config = getConfig(),
            appVersion = BuildConfig.VERSION_NAME,
            onThrowError = { throwable ->
                AnalyticsHelper.trackException(
                    event = com.paymentoptions.pos.analytics.AnalyticsEvent.APP_CRASH,
                    throwable = throwable,
                )
            },
        )
    }

    private fun initDatadogRum() {
        val applicationId = BuildConfig.DATADOG_APP_ID
        val clientToken = BuildConfig.DATADOG_CLIENT_TOKEN
        if (applicationId.isBlank() || clientToken.isBlank()) {
            AppLogger.warn("Datadog RUM skipped: DATADOG_APP_ID or DATADOG_CLIENT_TOKEN is missing")
            return
        }

        val environmentName = BuildConfig.ENVIRONMENT.lowercase()
        val appVariantName = "${BuildConfig.FLAVOR}-${BuildConfig.BUILD_TYPE}"

        val configuration = Configuration.Builder(
            clientToken = clientToken,
            env = environmentName,
            variant = appVariantName,
        )
            .useSite(DatadogSite.US1)
            .build()

        Datadog.initialize(this, configuration, TrackingConsent.GRANTED)

        val rumConfiguration = RumConfiguration.Builder(applicationId)
            .trackUserInteractions()
            .trackLongTasks()
            .useViewTrackingStrategy(ActivityViewTrackingStrategy(false))
            .build()

        Rum.enable(rumConfiguration)
        AppLogger.info("Datadog RUM initialized for env=$environmentName variant=$appVariantName")
    }

    private fun getConfig(): Config {
        val logFolderPath = "${cacheDir.absolutePath}${File.separator}daspay"
        val logConfig = LogConfig("", logFolderPath)
        return Config(logConfig = logConfig)
    }


}
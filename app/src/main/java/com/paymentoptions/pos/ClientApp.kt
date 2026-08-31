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
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.logger.Config
import com.paymentoptions.pos.logger.LogConfig
import com.paymentoptions.pos.services.analytics.AppAnalytics
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
        initializeDatadog()

        // Log the current environment
        AppLogger.info("App started - Build variant: ${BuildConfig.BUILD_TYPE}, Flavor: ${BuildConfig.FLAVOR}")
        AppAnalytics.appLaunch(variant = "${BuildConfig.FLAVOR}-${BuildConfig.BUILD_TYPE}")

        appScope.launch {
            try {
                AppLogger.info("MineSec bootstrap started")
                val hasLicenseAsset = try {
                    assets.list("")?.contains("payment-options.license") == true
                } catch (e: Exception) {
                    AppLogger.error("MineSec bootstrap: failed while checking license asset presence: ${e.message}", e)
                    false
                }

                AppLogger.debug("MineSec bootstrap context: package=${packageName}, " +
                        "process=${android.os.Process.myPid()}, flavor=${BuildConfig.FLAVOR}, " +
                        "buildType=${BuildConfig.BUILD_TYPE}, licenseAssetPresent=$hasLicenseAsset")

                AppAnalytics.mineSecSdkInitialization(step = "init_soft_pos", result = "started")
                val clientAppInitRes =
                    HeadlessSetup.initSoftPos(this@ClientApp, "payment-options.license")
                when (clientAppInitRes) {
                    is WrappedResult.Success -> {
                        AppLogger.info("MineSec initSoftPos success: ${clientAppInitRes.value}")
                    }

                    is WrappedResult.Failure -> {
                        AppLogger.error(
                            "MineSec initSoftPos failed: code=${clientAppInitRes.code}, message=${clientAppInitRes.message}, contextual=${clientAppInitRes.contextual}, extra=${clientAppInitRes.extra}"
                        )
                        Toast.makeText(
                            this@ClientApp,
                            "MineSec initialization failed: ${clientAppInitRes.message} with code ${clientAppInitRes.code}",
                            Toast.LENGTH_LONG
                        ).show()
                        if (clientAppInitRes.code == 987142) {
                            AppLogger.warn("MineSec initSoftPos failure code 987142 indicates SDK is not initialized; verify AAR compatibility and license loading")
                        }
                    }
                }
//                AppAnalytics.mineSecSdkInitialization(
//                    step = "init_soft_pos",
//                    result = if (clientAppInitRes is WrappedResult.Success) "success" else "failed",
//                    details = clientAppInitRes.toString()
//                )
//
//                AppAnalytics.mineSecSdkInitialization(step = "initial_setup", result = "started")
                AppLogger.debug("MineSec initialSetup started")
                val res = HeadlessSetup.initialSetup(this@ClientApp)
                AppLogger.debug("MineSec initialSetup response: $res")
                val setupFailureDetected = res.toString().contains("Failure(")
                if (setupFailureDetected) {
                    AppLogger.error("MineSec initialSetup contains failure result. If all entries fail with 987142, initSoftPos likely did not initialize the SDK runtime")
                } else {
                    AppLogger.info("MineSec initialSetup completed without reported failures")
                }
               // AppAnalytics.mineSecSdkInitialization(step = "initial_setup", result = "success")
                _sdkInitStatus.emit(clientAppInitRes)
            } catch (e: Exception) {
                AppLogger.error("MineSec bootstrap exception: ${e.message}", e)
                AppAnalytics.mineSecSdkInitialization(
                    step = "application_bootstrap",
                    result = "failed",
                    details = e.message
                )
                throw e
            }
        }

    }

    private fun initializeDatadog() {
        seedDatadogCredentialsIfMissing()

        val applicationId = DPSharedPreferences.getDatadogApplicationId(this)
        val clientToken = DPSharedPreferences.getDatadogClientToken(this)

        if (applicationId.isNullOrBlank() || clientToken.isNullOrBlank()) {
            AppLogger.warn("Datadog is disabled because encrypted credentials were not found")
            return
        }

        val environmentName = BuildConfig.ENVIRONMENT.lowercase()
        val appVariantName = "${BuildConfig.FLAVOR}-${BuildConfig.BUILD_TYPE}"

        val configuration = Configuration.Builder(
            clientToken = clientToken,
            env = environmentName,
            variant = appVariantName
        )
            .useSite(DatadogSite.US1)
            .build()

        Datadog.initialize(this, configuration, TrackingConsent.GRANTED)

        val rumConfiguration = RumConfiguration.Builder(applicationId)
            .trackUserInteractions()
            .trackLongTasks()
            .useViewTrackingStrategy(ActivityViewTrackingStrategy(true))
            .build()

        Rum.enable(rumConfiguration)
        AppLogger.info("Datadog RUM initialized for variant $appVariantName")
    }

    private fun seedDatadogCredentialsIfMissing() {
        val existingClientToken = DPSharedPreferences.getDatadogClientToken(this)
        val existingApplicationId = DPSharedPreferences.getDatadogApplicationId(this)

        if (!existingClientToken.isNullOrBlank() && !existingApplicationId.isNullOrBlank()) {
            return
        }

        val buildClientToken = BuildConfig.DATADOG_CLIENT_TOKEN
        val buildApplicationId = BuildConfig.DATADOG_APPLICATION_ID

        if (buildClientToken.isBlank() || buildApplicationId.isBlank()) {
            AppLogger.warn("Datadog BuildConfig credentials are empty; skipping secure credential seeding")
            return
        }

        DPSharedPreferences.saveDatadogCredentials(
            context = this,
            clientToken = buildClientToken,
            applicationId = buildApplicationId
        )
    }

    private fun initAppLogger() {
        AppLogger.init(
            context = this.applicationContext,
            config = getConfig(),
            appVersion = "Version 2.0",
            onThrowError = {
                AppAnalytics.appCrash(it, Thread.currentThread().name)
            },
        )
    }

    private fun getConfig(): Config {
        val logFolderPath = "${cacheDir.absolutePath}${File.separator}daspay"
        val logConfig = LogConfig("", logFolderPath)
        return Config(logConfig = logConfig)
    }


}
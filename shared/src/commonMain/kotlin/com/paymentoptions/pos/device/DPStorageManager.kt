package com.paymentoptions.pos.device

import com.paymentoptions.pos.network.AppConfig
import com.paymentoptions.pos.network.DevicePaymentMethod_Apms
import com.paymentoptions.pos.network.DevicePaymentMethod_Schemes
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.ExternalConfigurationResponse
import com.paymentoptions.pos.network.MerchantSetting
import com.paymentoptions.pos.network.SignInResponse
import com.paymentoptions.pos.payment.MineSecPlatform
import com.paymentoptions.pos.payment.reader.MerchantConfig
import com.paymentoptions.pos.payment.reader.ReaderLifecycleCoordinator
import com.paymentoptions.pos.storage.AppStorage
import kotlinx.serialization.json.Json

/**
 * Compatibility shim: all persistence delegates to AppStorage.
 */
object DPStorageManager {

    fun saveBoolean(key: String, value: Boolean) = dpSaveBoolean(key, value)

    fun getBoolean(key: String): Boolean = dpGetBoolean(key)

    fun saveKeyValue(key: String, value: String) = dpSaveKeyValue(key, value)

    fun getKeyValue(key: String): String? = dpGetKeyValue(key)

    fun saveBiometricsStatus(status: Boolean = false) = dpSaveBiometricsStatus(status)

    fun getBiometricsStatus(): Boolean = dpGetBiometricsStatus()

    fun isAdmin(): Boolean = dpIsAdmin()

    fun isStaff(): Boolean = dpIsStaff()

    fun saveAuthDetails(authDetails: SignInResponse) = dpSaveAuthDetails(authDetails)

    fun getAuthDetails(): SignInResponse? = dpGetAuthDetails()

    /**
     * Ends the session: auth data plus the device token, device config and biometrics, so the next
     * launch starts from sign-in and token verification again (as on release).
     */
    fun clearSharedPreferences() {
        dpClearSharedPreferences()
        AppStorage.tokenCode = null
        AppStorage.tokenVerified = false
        AppStorage.deviceConfigJson = null
        AppStorage.isBiometricEnabled = false
        ReaderLifecycleCoordinator.sessionEnded()
    }

    /** Wipes every persisted value (used when auto sign-in / session restore fails). */
    fun clearAllAndEndSession() {
        AppStorage.clearAll()
        ReaderLifecycleCoordinator.sessionEnded()
    }

    fun saveCredentials(email: String, password: String) = dpSaveCredentials(email, password)

    fun getSavedCredentials(): Triple<String?, String?, String?> = dpGetSavedCredentials()

    fun saveTokenStatus(tokenCode: String, isVerified: Boolean) = dpSaveTokenStatus(tokenCode, isVerified)

    fun getTokenStatus(): Pair<Boolean, String> = dpGetTokenStatus()

    fun saveFcmToken(token: String) = dpSaveFcmToken(token)

    fun getFcmToken(): String? = dpGetFcmToken()

    fun saveDeviceConfiguration(config: ExternalConfigurationResponse) {
        dpSaveDeviceConfiguration(config)

        val deviceInfo = config.data?.deviceInfo
        if (deviceInfo != null) {

//            let businessName = "Pertok"
//            let merchantId = "M1770891455"
//            let merchantCategoryCode = "5999"
//            let terminalEmvProfileId = "4c702000-0000-0000-03c9-91c46a350fd8"

            val merchantConfig = MerchantConfig(
                profileId = deviceInfo.MinesecDeviceId?.takeIf { it.isNotBlank() } ?: MineSecPlatform.PROFILE_ID,
                merchantBusinessName = "Pertok",
                merchantId = deviceInfo.MinesecMerchantId ?: "",
                merchantCategoryCode = "5999",
                terminalEmvProfileId = "4c702000-0000-0000-03c9-91c46a350fd8",
            )
            AppStorage.merchantConfigJson = Json.encodeToString(MerchantConfig.serializer(), merchantConfig)
            ReaderLifecycleCoordinator.sessionEstablished(merchantConfig)
        }
    }

    fun getDeviceConfiguration(): ExternalConfigurationResponse? = dpGetDeviceConfiguration()

    fun getMerchantConfig(): MerchantConfig? =
        AppStorage.merchantConfigJson?.let { Json.decodeFromString(MerchantConfig.serializer(), it) }

    fun getTransactionCurrency(): String = dpGetTransactionCurrency()

    fun getSettlementCurrency(): String = dpGetSettlementCurrency()

    fun getTapPayDasmid(): String = dpGetTapPayDasmid()

    fun getQRDasmid(): String = dpGetQRDasmid()

    fun getPayByLinkDasmid(): String = dpGetPayByLinkDasmid()

    fun getSchemes(): DevicePaymentMethod_Schemes = dpGetSchemes()

    fun getApms(): DevicePaymentMethod_Apms = dpGetApms()

    fun getMerchantSettings(): MerchantSetting? = getDeviceConfiguration()?.data?.deviceInfo?.MerchantSetting

    /**
     * MineSec profile for this merchant: the device config's MinesecDeviceId, falling back to the
     * app's built-in profile (MineSecPlatform.PROFILE_ID) when the config doesn't have one.
     */
    fun getMerchantProfileId(): String =
        getDeviceConfiguration()?.data?.deviceInfo?.MinesecDeviceId?.takeIf { it.isNotBlank() }
            ?: MineSecPlatform.PROFILE_ID

    /** Saves the config URLs; blank values are skipped so they never overwrite a good saved URL. */
    fun storeAppConfig(appConfig: AppConfig) {
        dpStoreAppConfig(appConfig)
        appConfig.ConfigJSON?.PayByLinkURL?.takeIf { it.isNotBlank() }?.let {
            AppStorage.putString(KEY_PAY_BY_LINK_URL, it)
        }
    }

    fun getTokenExpiry(): Long = dpGetTokenExpiry()

    /** Saved config base URL, or null before config has been downloaded (callers fall back to the flavour's URL). */
    fun getBaseUrl(): String? = dpGetBaseUrl()

    /** Saved transaction-details URL without a trailing slash, or the flavour default before config is downloaded. */
    fun getTransactionDetailsUrl(): String? =
        (dpGetTransactionDetailsUrl() ?: defaultTransactionDetailsUrl(ConfigurationManager.buildEnvironment))
            ?.trimEnd('/')

    /** Pay-by-link base URL ending in `/`: saved config value, or the flavour default. */
    fun getPayByLinkUrl(): String {
        val saved = AppStorage.getString(KEY_PAY_BY_LINK_URL)?.takeIf { it.isNotBlank() }
            ?: return defaultPayByLinkUrl(ConfigurationManager.buildEnvironment)
        val withScheme = if (saved.startsWith("https://")) saved else "https://$saved"
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }

    fun saveCvmLimit(limit: Float) = AppStorage.putString(KEY_CVM_LIMIT, limit.toString())

    fun getCvmLimit(): Float = AppStorage.getString(KEY_CVM_LIMIT)?.toFloatOrNull() ?: DEFAULT_CVM_LIMIT

    private const val KEY_PAY_BY_LINK_URL = "PayByLinkURL"
    private const val KEY_CVM_LIMIT = "cvm_limit"
    private const val DEFAULT_CVM_LIMIT = 200.0f
}

// Used only until the app config has been downloaded and saved.
private fun defaultPayByLinkUrl(environment: String): String = when (environment.uppercase()) {
    "PROD", "PRODUCTION" -> "https://api.paymentoptions.com/api/v1/paybylinkopen/"
    "UAT" -> "https://api-uat.paymentoptions.com/paybylink/"
    else -> "https://api-dev.paymentoptions.com/paybylink/"
}

// Only the dev URL is known; UAT and production rely on the downloaded config.
private fun defaultTransactionDetailsUrl(environment: String): String? = when (environment.uppercase()) {
    "DEV" -> "https://dev.paymentoptions.com/daspay-transaction-details/"
    else -> null
}

internal expect fun dpSaveBoolean(key: String, value: Boolean)
internal expect fun dpGetBoolean(key: String): Boolean
internal expect fun dpSaveKeyValue(key: String, value: String)
internal expect fun dpGetKeyValue(key: String): String?
internal expect fun dpSaveBiometricsStatus(status: Boolean)
internal expect fun dpGetBiometricsStatus(): Boolean
internal expect fun dpIsAdmin(): Boolean
internal expect fun dpIsStaff(): Boolean
internal expect fun dpSaveAuthDetails(authDetails: SignInResponse)
internal expect fun dpGetAuthDetails(): SignInResponse?
internal expect fun dpClearSharedPreferences()
internal expect fun dpSaveCredentials(email: String, password: String)
internal expect fun dpGetSavedCredentials(): Triple<String?, String?, String?>
internal expect fun dpSaveTokenStatus(tokenCode: String, isVerified: Boolean)
internal expect fun dpGetTokenStatus(): Pair<Boolean, String>
internal expect fun dpSaveFcmToken(token: String)
internal expect fun dpGetFcmToken(): String?
internal expect fun dpSaveDeviceConfiguration(config: ExternalConfigurationResponse)
internal expect fun dpGetDeviceConfiguration(): ExternalConfigurationResponse?
internal expect fun dpGetTransactionCurrency(): String
internal expect fun dpGetSettlementCurrency(): String
internal expect fun dpGetTapPayDasmid(): String
internal expect fun dpGetQRDasmid(): String
internal expect fun dpGetPayByLinkDasmid(): String
internal expect fun dpGetSchemes(): DevicePaymentMethod_Schemes
internal expect fun dpGetApms(): DevicePaymentMethod_Apms
internal expect fun dpStoreAppConfig(appConfig: AppConfig)
internal expect fun dpGetTokenExpiry(): Long
internal expect fun dpGetBaseUrl(): String?
internal expect fun dpGetTransactionDetailsUrl(): String?

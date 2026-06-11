package com.paymentoptions.pos.device

import com.paymentoptions.pos.network.AccessLevel
import com.paymentoptions.pos.network.AppConfig
import com.paymentoptions.pos.network.DevicePaymentMethod_Apms
import com.paymentoptions.pos.network.DevicePaymentMethod_Schemes
import com.paymentoptions.pos.network.ExternalConfigurationResponse
import com.paymentoptions.pos.network.SignInResponse
import com.paymentoptions.pos.platformLog
import com.paymentoptions.pos.platformLogError
import com.paymentoptions.pos.storage.AppStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Compatibility shim — all persistence now delegates to [AppStorage].
 * The `context` parameter is kept for call-site compatibility but is unused.
 * Prefer calling [AppStorage] directly in new code.
 */
object DPStorageManager {

    private const val TAG = "DPStorageManager"
    private val json = Json { ignoreUnknownKeys = true }

    fun saveBoolean(key: String, value: Boolean) {
        AppStorage.putBoolean(key, value)
    }

    fun getBoolean(key: String): Boolean =
        AppStorage.getBoolean(key)

    fun saveKeyValue(key: String, value: String) {
        AppStorage.putString(key, value)
    }

    fun getKeyValue(key: String): String? =
        AppStorage.getString(key)

    fun saveBiometricsStatus(status: Boolean = false) {
        AppStorage.isBiometricEnabled = status
    }

    fun getBiometricsStatus(): Boolean = AppStorage.isBiometricEnabled

//    fun getImmersiveModeStatus(context: Any? = null): Boolean =
//        AppStorage.getBoolean("immersive")
//
//    fun saveImmersiveModeStatus(context: Any? = null, status: Boolean = false) {
//        AppStorage.putBoolean("immersive", status)
//    }

    fun isAdmin(): Boolean =
        AppStorage.accessLevel == AccessLevel.ADMIN.name

    fun isStaff(): Boolean =
        AppStorage.accessLevel == AccessLevel.STAFF.name

    fun saveAuthDetails(authDetails: SignInResponse) {
        AppStorage.authDetailsJson = json.encodeToString(authDetails)
        if (authDetails.data == null) {
            platformLogError(TAG, "authDetails.data is null")
        }
        platformLog(TAG, "authDetails.data: ${authDetails.data}")
        val data = authDetails.data ?: return
        AppStorage.accessToken      = data.token.accessToken
        AppStorage.idToken          = data.token.idToken
        AppStorage.refreshToken     = data.token.refreshToken
        AppStorage.tokenExpiry      = data.exp * 1000L
        AppStorage.userEmail        = data.email
        AppStorage.userName         = data.name
        AppStorage.userUid          = data.uid
        AppStorage.accessLevel      = data.accessLevel?.name
        AppStorage.signInAsMerchant = data.signInAsMerchant
    }

    fun getAuthDetails(): SignInResponse? {
        platformLog(TAG, "Retrieving auth details: ")
        return AppStorage.authDetailsJson?.let { json.decodeFromString(it) }
    }

    fun clearSharedPreferences() {
        platformLog(TAG, "Clearing auth data from shared preferences")
        AppStorage.clearAuthData()
    }

    fun saveCredentials(email: String, password: String) {
        AppStorage.savedUsername = email
        AppStorage.savedPassword = password
    }

    fun getSavedCredentials(): Triple<String?, String?, String?> =
        Triple(AppStorage.savedUsername, AppStorage.savedPassword, AppStorage.tokenCode)

    fun saveTokenStatus(tokenCode: String, isVerified: Boolean) {
        AppStorage.tokenCode     = tokenCode
        AppStorage.tokenVerified = isVerified
    }

    fun getTokenStatus(): Pair<Boolean, String> =
        Pair(AppStorage.tokenVerified, AppStorage.tokenCode ?: "")

    fun saveFcmToken(token: String) {
        AppStorage.fcmToken = token
    }

    fun getFcmToken(): String? = AppStorage.fcmToken

    fun saveDeviceConfiguration(config: ExternalConfigurationResponse) {
        AppStorage.deviceConfigJson = json.encodeToString(config)
    }

    fun getDeviceConfiguration(): ExternalConfigurationResponse? =
        AppStorage.deviceConfigJson?.let { json.decodeFromString(it) }

    fun getTransactionCurrency(): String {
        val config = getDeviceConfiguration() ?: return ""
        return config.data?.paymentMethod?.firstOrNull()?.TransactionCCY?.firstOrNull() ?: ""
    }

    fun getSettlementCurrency(): String {
        val config = getDeviceConfiguration() ?: return ""
        return config.data?.paymentMethod?.firstOrNull()?.SettlementCCY ?: ""
    }

    fun getTapPayDasmid(): String {
        val config = getDeviceConfiguration() ?: return ""
        return config.data?.paymentMethod?.firstOrNull { it.Type == "SOFTPOS" }?.DASMID ?: ""
    }

    fun getQRDasmid(): String {
        val config = getDeviceConfiguration() ?: return ""
        return config.data?.paymentMethod?.firstOrNull { it.Type == "QR" }?.DASMID ?: ""
    }

    fun getPayByLinkDasmid(): String {
        val config = getDeviceConfiguration() ?: return ""
        return config.data?.paymentMethod?.firstOrNull { it.Type == "PBL" }?.DASMID ?: ""
    }

    fun getSchemes(): DevicePaymentMethod_Schemes {
        val config = getDeviceConfiguration() ?: return DevicePaymentMethod_Schemes()
        return config.data?.paymentMethod?.firstOrNull { it.Type == "SOFTPOS" }?.schemes
            ?: DevicePaymentMethod_Schemes()
    }

    fun getApms(): DevicePaymentMethod_Apms {
        val config = getDeviceConfiguration() ?: return DevicePaymentMethod_Apms()
        return config.data?.paymentMethod?.firstOrNull { it.Type == "QR" }?.apms
            ?: DevicePaymentMethod_Apms()
    }

//    fun getDeviceId(context: Any? = null): String? {
//        return getDeviceConfiguration()?.data?.deviceInfo?.DeviceID
//    }

    fun storeAppConfig(appConfig: AppConfig) {
        AppStorage.baseUrl               = appConfig.BaseAPIURL
        AppStorage.transactionDetailsUrl = appConfig.TransactionDetailsURL
    }

    fun getBaseUrl(): String? = AppStorage.baseUrl

    fun getTransactionDetailsUrl(): String? = AppStorage.transactionDetailsUrl
}

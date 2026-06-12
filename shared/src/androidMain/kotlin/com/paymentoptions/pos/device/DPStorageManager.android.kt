package com.paymentoptions.pos.device

import com.paymentoptions.pos.network.AccessLevel
import com.paymentoptions.pos.network.AppConfig
import com.paymentoptions.pos.network.DevicePaymentMethod_Apms
import com.paymentoptions.pos.network.DevicePaymentMethod_Schemes
import com.paymentoptions.pos.network.ExternalConfigurationResponse
import com.paymentoptions.pos.network.SignInResponse
import com.paymentoptions.pos.platformLog
import com.paymentoptions.pos.platformLogError
import com.paymentoptions.pos.storage.AndroidAppStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "DPStorageManager"
private val json = Json { ignoreUnknownKeys = true }
private val storage get() = AndroidAppStorage

internal actual fun dpSaveBoolean(key: String, value: Boolean) {
    storage.putBoolean(key, value)
}

internal actual fun dpGetBoolean(key: String): Boolean =
    storage.getBoolean(key)

internal actual fun dpSaveKeyValue(key: String, value: String) {
    storage.putString(key, value)
}

internal actual fun dpGetKeyValue(key: String): String? =
    storage.getString(key)

internal actual fun dpSaveBiometricsStatus(status: Boolean) {
    storage.isBiometricEnabled = status
}

internal actual fun dpGetBiometricsStatus(): Boolean = storage.isBiometricEnabled

internal actual fun dpIsAdmin(): Boolean =
    storage.accessLevel == AccessLevel.ADMIN.name

internal actual fun dpIsStaff(): Boolean =
    storage.accessLevel == AccessLevel.STAFF.name

internal actual fun dpSaveAuthDetails(authDetails: SignInResponse) {
    storage.authDetailsJson = json.encodeToString(authDetails)
    if (authDetails.data == null) {
        platformLogError(TAG, "authDetails.data is null")
    }
    platformLog(TAG, "authDetails.data: ${authDetails.data}")
    val data = authDetails.data ?: return
    storage.accessToken = data.token.accessToken
    storage.idToken = data.token.idToken
    storage.refreshToken = data.token.refreshToken
    storage.tokenExpiry = data.exp * 1000L
    storage.userEmail = data.email
    storage.userName = data.name
    storage.userUid = data.uid
    storage.accessLevel = data.accessLevel?.name
    storage.signInAsMerchant = data.signInAsMerchant
}

internal actual fun dpGetAuthDetails(): SignInResponse? {
    platformLog(TAG, "Retrieving auth details: ")
    return storage.authDetailsJson?.let { json.decodeFromString(it) }
}

internal actual fun dpClearSharedPreferences() {
    platformLog(TAG, "Clearing auth data from shared preferences")
    storage.clearAuthData()
}

internal actual fun dpSaveCredentials(email: String, password: String) {
    storage.savedUsername = email
    storage.savedPassword = password
}

internal actual fun dpGetSavedCredentials(): Triple<String?, String?, String?> =
    Triple(storage.savedUsername, storage.savedPassword, storage.tokenCode)

internal actual fun dpSaveTokenStatus(tokenCode: String, isVerified: Boolean) {
    storage.tokenCode = tokenCode
    storage.tokenVerified = isVerified
}

internal actual fun dpGetTokenStatus(): Pair<Boolean, String> =
    Pair(storage.tokenVerified, storage.tokenCode ?: "")

internal actual fun dpSaveFcmToken(token: String) {
    storage.fcmToken = token
}

internal actual fun dpGetFcmToken(): String? = storage.fcmToken

internal actual fun dpSaveDeviceConfiguration(config: ExternalConfigurationResponse) {
    storage.deviceConfigJson = json.encodeToString(config)
}

internal actual fun dpGetDeviceConfiguration(): ExternalConfigurationResponse? =
    storage.deviceConfigJson?.let { json.decodeFromString(it) }

internal actual fun dpGetTransactionCurrency(): String {
    val config = dpGetDeviceConfiguration() ?: return ""
    return config.data?.paymentMethod?.firstOrNull()?.TransactionCCY?.firstOrNull() ?: ""
}

internal actual fun dpGetSettlementCurrency(): String {
    val config = dpGetDeviceConfiguration() ?: return ""
    return config.data?.paymentMethod?.firstOrNull()?.SettlementCCY ?: ""
}

internal actual fun dpGetTapPayDasmid(): String {
    val config = dpGetDeviceConfiguration() ?: return ""
    return config.data?.paymentMethod?.firstOrNull { it.Type == "SOFTPOS" }?.DASMID ?: ""
}

internal actual fun dpGetQRDasmid(): String {
    val config = dpGetDeviceConfiguration() ?: return ""
    return config.data?.paymentMethod?.firstOrNull { it.Type == "QR" }?.DASMID ?: ""
}

internal actual fun dpGetPayByLinkDasmid(): String {
    val config = dpGetDeviceConfiguration() ?: return ""
    return config.data?.paymentMethod?.firstOrNull { it.Type == "PBL" }?.DASMID ?: ""
}

internal actual fun dpGetSchemes(): DevicePaymentMethod_Schemes {
    val config = dpGetDeviceConfiguration() ?: return DevicePaymentMethod_Schemes()
    return config.data?.paymentMethod?.firstOrNull { it.Type == "SOFTPOS" }?.schemes
        ?: DevicePaymentMethod_Schemes()
}

internal actual fun dpGetApms(): DevicePaymentMethod_Apms {
    val config = dpGetDeviceConfiguration() ?: return DevicePaymentMethod_Apms()
    return config.data?.paymentMethod?.firstOrNull { it.Type == "QR" }?.apms
        ?: DevicePaymentMethod_Apms()
}

internal actual fun dpStoreAppConfig(appConfig: AppConfig) {
    storage.baseUrl = appConfig.BaseAPIURL
    storage.transactionDetailsUrl = appConfig.TransactionDetailsURL
}

internal actual fun dpGetTokenExpiry(): Long = storage.tokenExpiry

internal actual fun dpGetBaseUrl(): String? = storage.baseUrl

internal actual fun dpGetTransactionDetailsUrl(): String? = storage.transactionDetailsUrl

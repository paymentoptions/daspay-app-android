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

private const val TAG = "DPStorageManager"
private val json = Json { ignoreUnknownKeys = true }

internal actual fun dpSaveBoolean(key: String, value: Boolean) {
    AppStorage.putBoolean(key, value)
}

internal actual fun dpGetBoolean(key: String): Boolean =
    AppStorage.getBoolean(key)

internal actual fun dpSaveKeyValue(key: String, value: String) {
    AppStorage.putString(key, value)
}

internal actual fun dpGetKeyValue(key: String): String? =
    AppStorage.getString(key)

internal actual fun dpSaveBiometricsStatus(status: Boolean) {
    AppStorage.isBiometricEnabled = status
}

internal actual fun dpGetBiometricsStatus(): Boolean = AppStorage.isBiometricEnabled

internal actual fun dpIsAdmin(): Boolean =
    AppStorage.accessLevel == AccessLevel.ADMIN.name

internal actual fun dpIsStaff(): Boolean =
    AppStorage.accessLevel == AccessLevel.STAFF.name

internal actual fun dpSaveAuthDetails(authDetails: SignInResponse) {
    AppStorage.authDetailsJson = json.encodeToString(authDetails)
    if (authDetails.data == null) {
        platformLogError(TAG, "authDetails.data is null")
    }
    platformLog(TAG, "authDetails.data: ${authDetails.data}")
    val data = authDetails.data ?: return
    AppStorage.accessToken = data.token.accessToken
    AppStorage.idToken = data.token.idToken
    AppStorage.refreshToken = data.token.refreshToken
    AppStorage.tokenExpiry = data.exp * 1000L
    AppStorage.userEmail = data.email
    AppStorage.userName = data.name
    AppStorage.userUid = data.uid
    AppStorage.accessLevel = data.accessLevel?.name
    AppStorage.signInAsMerchant = data.signInAsMerchant
}

internal actual fun dpGetAuthDetails(): SignInResponse? {
    platformLog(TAG, "Retrieving auth details: ")
    return AppStorage.authDetailsJson?.let { json.decodeFromString(it) }
}

internal actual fun dpClearSharedPreferences() {
    platformLog(TAG, "Clearing auth data from shared preferences")
    AppStorage.clearAuthData()
}

internal actual fun dpSaveCredentials(email: String, password: String) {
    AppStorage.savedUsername = email
    AppStorage.savedPassword = password
}

internal actual fun dpGetSavedCredentials(): Triple<String?, String?, String?> =
    Triple(AppStorage.savedUsername, AppStorage.savedPassword, AppStorage.tokenCode)

internal actual fun dpSaveTokenStatus(tokenCode: String, isVerified: Boolean) {
    AppStorage.tokenCode = tokenCode
    AppStorage.tokenVerified = isVerified
}

internal actual fun dpGetTokenStatus(): Pair<Boolean, String> =
    Pair(AppStorage.tokenVerified, AppStorage.tokenCode ?: "")

internal actual fun dpSaveFcmToken(token: String) {
    AppStorage.fcmToken = token
}

internal actual fun dpGetFcmToken(): String? = AppStorage.fcmToken

internal actual fun dpSaveDeviceConfiguration(config: ExternalConfigurationResponse) {
    AppStorage.deviceConfigJson = json.encodeToString(config)
}

internal actual fun dpGetDeviceConfiguration(): ExternalConfigurationResponse? =
    AppStorage.deviceConfigJson?.let { json.decodeFromString(it) }

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
    AppStorage.baseUrl = appConfig.BaseAPIURL
    AppStorage.transactionDetailsUrl = appConfig.TransactionDetailsURL
}

internal actual fun dpGetTokenExpiry(): Long = AppStorage.tokenExpiry

internal actual fun dpGetBaseUrl(): String? = AppStorage.baseUrl

internal actual fun dpGetTransactionDetailsUrl(): String? = AppStorage.transactionDetailsUrl

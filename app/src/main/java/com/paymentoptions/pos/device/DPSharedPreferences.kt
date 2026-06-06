package com.paymentoptions.pos.device

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.network.AccessLevel
import com.paymentoptions.pos.network.AppConfig
import com.paymentoptions.pos.network.DevicePaymentMethod_Apms
import com.paymentoptions.pos.network.DevicePaymentMethod_Schemes
import com.paymentoptions.pos.network.ExternalConfigurationResponse
import com.paymentoptions.pos.network.SignInResponse
import com.paymentoptions.pos.platformLog
import com.paymentoptions.pos.storage.AppStorage
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.Cart
import com.paymentoptions.pos.utils.PaymentMethod
import com.paymentoptions.pos.utils.qrCodePaymentMethod
import com.paymentoptions.pos.utils.tapPaymentMethod
import com.paymentoptions.pos.utils.viaLinkPaymentMethod
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Compatibility shim — all persistence now delegates to [AppStorage].
 * The Context parameter is kept for call-site compatibility but is unused.
 */
object DPSharedPreferences {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Generic key-value ─────────────────────────────────────────────────────

    fun saveBoolean(context: Context, key: String, value: Boolean) {
        AppStorage.putBoolean(key, value)
    }

    fun getBoolean(context: Context, key: String): Boolean =
        AppStorage.getBoolean(key)

    fun saveKeyValue(context: Context, key: String, value: String) {
        AppStorage.putString(key, value)
    }

    fun getKeyValue(context: Context, key: String): String? =
        AppStorage.getString(key)

    // ── Biometrics ────────────────────────────────────────────────────────────

    fun saveBiometricsStatus(context: Context, status: Boolean = false) {
        AppStorage.isBiometricEnabled = status
    }

    fun getBiometricsStatus(context: Context): Boolean = AppStorage.isBiometricEnabled

    // ── Immersive mode (kept for API compatibility, not actively used) ─────────

    fun getImmersiveModeStatus(context: Context): Boolean =
        AppStorage.getBoolean("immersive")

    fun saveImmersiveModeStatus(context: Context, status: Boolean = false) {
        AppStorage.putBoolean("immersive", status)
    }

    // ── Access-level helpers ──────────────────────────────────────────────────

    fun isAdmin(context: Context): Boolean =
        AppStorage.accessLevel == AccessLevel.ADMIN.name

    fun isStaff(context: Context): Boolean =
        AppStorage.accessLevel == AccessLevel.STAFF.name

    // ── Auth ──────────────────────────────────────────────────────────────────

    fun saveAuthDetails(context: Context, authDetails: SignInResponse) {
        AppStorage.authDetailsJson = json.encodeToString(authDetails)
        if(authDetails.data == null){
            AppLogger.error("authDetails.data is null")
        }
        AppLogger.debug("authDetails.data: ${authDetails.data}")
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

    fun getAuthDetails(context: Context): SignInResponse? {
        platformLog("DPSharedPreferences", "Retrieving auth details: ")
        return AppStorage.authDetailsJson?.let { json.decodeFromString(it) }
    }

    fun clearSharedPreferences(context: Context) {
        AppLogger.debug(Thread.currentThread().stackTrace[4],"Clearing auth data from shared preferences")
        AppStorage.clearAuthData()
    }

    // ── Credentials ───────────────────────────────────────────────────────────

    fun saveCredentials(context: Context, email: String, password: String) {
        AppStorage.savedUsername = email
        AppStorage.savedPassword = password
    }

    fun getSavedCredentials(context: Context): Triple<String?, String?, String?> =
        Triple(AppStorage.savedUsername, AppStorage.savedPassword, AppStorage.tokenCode)

    // ── Token verification (device OTP) ──────────────────────────────────────

    fun saveTokenStatus(context: Context, tokenCode: String, isVerified: Boolean) {
        AppStorage.tokenCode     = tokenCode
        AppStorage.tokenVerified = isVerified
    }

    fun getTokenStatus(context: Context): Pair<Boolean, String> =
        Pair(AppStorage.tokenVerified, AppStorage.tokenCode ?: "")

    // ── FCM ───────────────────────────────────────────────────────────────────

    fun saveFcmToken(context: Context, token: String) {
        AppStorage.fcmToken = token
    }

    fun getFcmToken(context: Context): String? = AppStorage.fcmToken

    // ── Cart ──────────────────────────────────────────────────────────────────

    fun getCart(context: Context): Cart? =
        AppStorage.cartJson?.let { json.decodeFromString<Cart>(it) }

    fun saveCart(context: Context, cart: Cart) {
        AppStorage.cartJson = cart.toJson()
    }

    fun clearSavedCart(context: Context) {
        AppStorage.cartJson = null
        AppLogger.debug("cart cleared")
    }

    // ── External device configuration ─────────────────────────────────────────

    fun saveDeviceConfiguration(context: Context, config: ExternalConfigurationResponse) {
        AppStorage.deviceConfigJson = json.encodeToString(config)
    }

    fun getDeviceConfiguration(context: Context): ExternalConfigurationResponse? =
        AppStorage.deviceConfigJson?.let { json.decodeFromString(it) }

    // ── Payment method helpers (derived from device config) ───────────────────

    fun getTransactionCurrency(context: Context): String {
        val config = getDeviceConfiguration(context) ?: return ""
        return config.data?.paymentMethod?.firstOrNull()?.TransactionCCY?.firstOrNull() ?: ""
    }

    fun getSettlementCurrency(context: Context): String {
        val config = getDeviceConfiguration(context) ?: return ""
        return config.data?.paymentMethod?.firstOrNull()?.SettlementCCY ?: ""
    }

    fun getTapPayDasmid(context: Context): String {
        val config = getDeviceConfiguration(context) ?: return ""
        return config.data?.paymentMethod?.firstOrNull { it.Type == "SOFTPOS" }?.DASMID ?: ""
    }

    fun getQRDasmid(context: Context): String {
        val config = getDeviceConfiguration(context) ?: return ""
        return config.data?.paymentMethod?.firstOrNull { it.Type == "QR" }?.DASMID ?: ""
    }

    fun getPayByLinkDasmid(context: Context): String {
        val config = getDeviceConfiguration(context) ?: return ""
        return config.data?.paymentMethod?.firstOrNull { it.Type == "PBL" }?.DASMID ?: ""
    }

    fun getSchemes(context: Context): DevicePaymentMethod_Schemes {
        val config = getDeviceConfiguration(context) ?: return DevicePaymentMethod_Schemes()
        return config.data?.paymentMethod?.firstOrNull { it.Type == "SOFTPOS" }?.schemes
            ?: DevicePaymentMethod_Schemes()
    }

    fun getApms(context: Context): DevicePaymentMethod_Apms {
        val config = getDeviceConfiguration(context) ?: return DevicePaymentMethod_Apms()
        return config.data?.paymentMethod?.firstOrNull { it.Type == "QR" }?.apms
            ?: DevicePaymentMethod_Apms()
    }

    fun getDeviceId(context: Context): String? {
        return getDeviceConfiguration(context)?.data?.deviceInfo?.DeviceID
    }

    fun getAvailablePaymentsList(context: Context): List<PaymentMethod> {
        val config = getDeviceConfiguration(context) ?: return emptyList()
        val availableTypes = config.data?.paymentMethod?.map { it.Type }?.toSet() ?: emptySet()
        AppLogger.debug("available payments: $availableTypes")
        return buildList {
            if ("SOFTPOS" in availableTypes) add(tapPaymentMethod)
            if ("QR" in availableTypes)      add(qrCodePaymentMethod)
            if ("PBL" in availableTypes)     add(viaLinkPaymentMethod)
        }
    }

    // ── App config (base URL + transaction details URL) ───────────────────────

    fun storeAppConfig(context: Context, appConfig: AppConfig) {
        AppStorage.baseUrl               = appConfig.BaseAPIURL
        AppStorage.transactionDetailsUrl = appConfig.TransactionDetailsURL
    }

    fun getBaseUrl(context: Context): String? = AppStorage.baseUrl

    fun getTransactionDetailsUrl(context: Context): String? = AppStorage.transactionDetailsUrl
}

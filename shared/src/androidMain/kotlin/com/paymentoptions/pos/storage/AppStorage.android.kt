package com.paymentoptions.pos.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

actual object AppStorage {

    private lateinit var settings: Settings

    /**
     * Call once in MainActivity before any read/write.
     * Uses the provided [Settings] (backed by EncryptedSharedPreferences).
     */
    fun init(settings: Settings) {
        this.settings = settings
    }

    // ── Tokens ────────────────────────────────────────────────────────────────

    actual var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        set(v) = if (v != null) settings[KEY_ACCESS_TOKEN] = v else settings.remove(KEY_ACCESS_TOKEN)

    actual var idToken: String?
        get() = settings.getStringOrNull(KEY_ID_TOKEN)
        set(v) = if (v != null) settings[KEY_ID_TOKEN] = v else settings.remove(KEY_ID_TOKEN)

    actual var refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)
        set(v) = if (v != null) settings[KEY_REFRESH_TOKEN] = v else settings.remove(KEY_REFRESH_TOKEN)

    actual var tokenExpiry: Long
        get() = settings[KEY_TOKEN_EXPIRY, 0L]
        set(v) { settings[KEY_TOKEN_EXPIRY] = v }

    // ── Token verification ────────────────────────────────────────────────────

    actual var tokenVerified: Boolean
        get() = settings[KEY_TOKEN_VERIFIED, false]
        set(v) { settings[KEY_TOKEN_VERIFIED] = v }

    actual var tokenCode: String?
        get() = settings.getStringOrNull(KEY_TOKEN_CODE)
        set(v) = if (v != null) settings[KEY_TOKEN_CODE] = v else settings.remove(KEY_TOKEN_CODE)

    // ── Credentials ───────────────────────────────────────────────────────────

    actual var savedUsername: String?
        get() = settings.getStringOrNull(KEY_USERNAME)
        set(v) = if (v != null) settings[KEY_USERNAME] = v else settings.remove(KEY_USERNAME)

    actual var savedPassword: String?
        get() = settings.getStringOrNull(KEY_PASSWORD)
        set(v) = if (v != null) settings[KEY_PASSWORD] = v else settings.remove(KEY_PASSWORD)

    // ── User profile ──────────────────────────────────────────────────────────

    actual var userEmail: String?
        get() = settings.getStringOrNull(KEY_USER_EMAIL)
        set(v) = if (v != null) settings[KEY_USER_EMAIL] = v else settings.remove(KEY_USER_EMAIL)

    actual var userName: String?
        get() = settings.getStringOrNull(KEY_USER_NAME)
        set(v) = if (v != null) settings[KEY_USER_NAME] = v else settings.remove(KEY_USER_NAME)

    actual var userUid: String?
        get() = settings.getStringOrNull(KEY_USER_UID)
        set(v) = if (v != null) settings[KEY_USER_UID] = v else settings.remove(KEY_USER_UID)

    actual var accessLevel: String?
        get() = settings.getStringOrNull(KEY_ACCESS_LEVEL)
        set(v) = if (v != null) settings[KEY_ACCESS_LEVEL] = v else settings.remove(KEY_ACCESS_LEVEL)

    actual var signInAsMerchant: Boolean
        get() = settings[KEY_SIGN_IN_AS_MERCHANT, false]
        set(v) { settings[KEY_SIGN_IN_AS_MERCHANT] = v }

    // ── Auth details JSON ─────────────────────────────────────────────────────

    actual var authDetailsJson: String?
        get() = settings.getStringOrNull(KEY_AUTH_DETAILS_JSON)
        set(v) = if (v != null) settings[KEY_AUTH_DETAILS_JSON] = v else settings.remove(KEY_AUTH_DETAILS_JSON)

    // ── Device registration ───────────────────────────────────────────────────

    actual var deviceNumber: String?
        get() = settings.getStringOrNull(KEY_DEVICE_NUMBER)
        set(v) = if (v != null) settings[KEY_DEVICE_NUMBER] = v else settings.remove(KEY_DEVICE_NUMBER)

    actual var deviceUniqueCode: String?
        get() = settings.getStringOrNull(KEY_DEVICE_UNIQUE_CODE)
        set(v) = if (v != null) settings[KEY_DEVICE_UNIQUE_CODE] = v else settings.remove(KEY_DEVICE_UNIQUE_CODE)

    actual var dasmid: String?
        get() = settings.getStringOrNull(KEY_DASMID)
        set(v) = if (v != null) settings[KEY_DASMID] = v else settings.remove(KEY_DASMID)

    actual var merchantLegalName: String?
        get() = settings.getStringOrNull(KEY_MERCHANT_LEGAL_NAME)
        set(v) = if (v != null) settings[KEY_MERCHANT_LEGAL_NAME] = v else settings.remove(KEY_MERCHANT_LEGAL_NAME)

    // ── Device / external config JSON ─────────────────────────────────────────

    actual var deviceConfigJson: String?
        get() = settings.getStringOrNull(KEY_DEVICE_CONFIG_JSON)
        set(v) = if (v != null) settings[KEY_DEVICE_CONFIG_JSON] = v else settings.remove(KEY_DEVICE_CONFIG_JSON)

    // ── App config / URLs ─────────────────────────────────────────────────────

    actual var baseUrl: String?
        get() = settings.getStringOrNull(KEY_BASE_URL)
        set(v) = if (v != null) settings[KEY_BASE_URL] = v else settings.remove(KEY_BASE_URL)

    actual var configBaseUrl: String?
        get() = settings.getStringOrNull(KEY_CONFIG_BASE_URL)
        set(v) = if (v != null) settings[KEY_CONFIG_BASE_URL] = v else settings.remove(KEY_CONFIG_BASE_URL)

    actual var transactionDetailsUrl: String?
        get() = settings.getStringOrNull(KEY_TRANSACTION_DETAILS_URL)
        set(v) = if (v != null) settings[KEY_TRANSACTION_DETAILS_URL] = v else settings.remove(KEY_TRANSACTION_DETAILS_URL)

    // ── FCM ───────────────────────────────────────────────────────────────────

    actual var fcmToken: String?
        get() = settings.getStringOrNull(KEY_FCM_TOKEN)
        set(v) = if (v != null) settings[KEY_FCM_TOKEN] = v else settings.remove(KEY_FCM_TOKEN)

    // ── Cart ──────────────────────────────────────────────────────────────────

    actual var cartJson: String?
        get() = settings.getStringOrNull(KEY_CART_JSON)
        set(v) = if (v != null) settings[KEY_CART_JSON] = v else settings.remove(KEY_CART_JSON)

    // ── Payment methods ───────────────────────────────────────────────────────

    actual var paymentMethodsJson: String?
        get() = settings.getStringOrNull(KEY_PAYMENT_METHODS_JSON)
        set(v) = if (v != null) settings[KEY_PAYMENT_METHODS_JSON] = v else settings.remove(KEY_PAYMENT_METHODS_JSON)

    actual var externalConfigJson: String?
        get() = settings.getStringOrNull(KEY_EXTERNAL_CONFIG_JSON)
        set(v) = if (v != null) settings[KEY_EXTERNAL_CONFIG_JSON] = v else settings.remove(KEY_EXTERNAL_CONFIG_JSON)

    // ── Geo restriction ───────────────────────────────────────────────────────

    actual var merchantCountriesJson: String?
        get() = settings.getStringOrNull(KEY_MERCHANT_COUNTRIES_JSON)
        set(v) = if (v != null) settings[KEY_MERCHANT_COUNTRIES_JSON] = v else settings.remove(KEY_MERCHANT_COUNTRIES_JSON)

    // ── Biometric ─────────────────────────────────────────────────────────────

    actual var isBiometricEnabled: Boolean
        get() = settings[KEY_BIOMETRIC_ENABLED, false]
        set(v) { settings[KEY_BIOMETRIC_ENABLED] = v }

    // ── Generic helpers ───────────────────────────────────────────────────────

    actual fun getString(key: String): String? = settings.getStringOrNull(key)
    actual fun putString(key: String, value: String) { settings[key] = value }
    actual fun getBoolean(key: String, default: Boolean): Boolean = settings[key, default]
    actual fun putBoolean(key: String, value: Boolean) { settings[key] = value }

    // ── Helpers ───────────────────────────────────────────────────────────────

    actual fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()

    actual fun clearAuthData() {
        accessToken     = null
        idToken         = null
        refreshToken    = null
        tokenExpiry     = 0L
        userEmail       = null
        userName        = null
        userUid         = null
        accessLevel     = null
        authDetailsJson = null
    }

    actual fun clearAll() {
        settings.clear()
    }

    // ── Keys ──────────────────────────────────────────────────────────────────

    private const val KEY_ACCESS_TOKEN            = "accessToken"
    private const val KEY_ID_TOKEN                = "idToken"
    private const val KEY_REFRESH_TOKEN           = "refreshToken"
    private const val KEY_TOKEN_EXPIRY            = "tokenExpiry"
    private const val KEY_TOKEN_VERIFIED          = "tokenVerified"
    private const val KEY_TOKEN_CODE              = "tokenCode"
    private const val KEY_USERNAME                = "savedUsername"
    private const val KEY_PASSWORD                = "savedPassword"
    private const val KEY_USER_EMAIL              = "userEmail"
    private const val KEY_USER_NAME               = "userName"
    private const val KEY_USER_UID                = "userUid"
    private const val KEY_ACCESS_LEVEL            = "accessLevel"
    private const val KEY_SIGN_IN_AS_MERCHANT     = "signInAsMerchant"
    private const val KEY_AUTH_DETAILS_JSON       = "authDetailsJson"
    private const val KEY_DEVICE_NUMBER           = "deviceNumber"
    private const val KEY_DEVICE_UNIQUE_CODE      = "deviceUniqueCode"
    private const val KEY_DASMID                  = "dasmid"
    private const val KEY_MERCHANT_LEGAL_NAME     = "merchantLegalName"
    private const val KEY_DEVICE_CONFIG_JSON      = "deviceConfigJson"
    private const val KEY_BASE_URL                = "baseUrl"
    private const val KEY_CONFIG_BASE_URL         = "configBaseUrl"
    private const val KEY_TRANSACTION_DETAILS_URL = "transactionDetailsUrl"
    private const val KEY_FCM_TOKEN               = "fcmToken"
    private const val KEY_CART_JSON               = "cartJson"
    private const val KEY_PAYMENT_METHODS_JSON    = "paymentMethodsJson"
    private const val KEY_EXTERNAL_CONFIG_JSON    = "externalConfigJson"
    private const val KEY_MERCHANT_COUNTRIES_JSON = "merchantCountriesJson"
    private const val KEY_BIOMETRIC_ENABLED       = "biometricEnabled"
}

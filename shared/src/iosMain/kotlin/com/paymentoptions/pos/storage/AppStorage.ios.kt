package com.paymentoptions.pos.storage

import io.ktor.utils.io.charsets.Charsets
import platform.Foundation.NSUserDefaults
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.nativeHeap
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

/**
 * iOS actual — sensitive fields (tokens, password) go to Keychain.
 * Non-sensitive config/UI fields use NSUserDefaults.
 */
@OptIn(ExperimentalForeignApi::class)
actual object AppStorage {

    private val defaults = NSUserDefaults.standardUserDefaults
    private const val SERVICE = "com.paymentoptions.pos"

    // ── Keychain helpers ──────────────────────────────────────────────────────

    private fun keychainWrite(key: String, value: String) {
        val data = value.encodeToByteArray().toNSData()
        val query = mapOf(
            kSecClass        to kSecClassGenericPassword,
            kSecAttrService  to SERVICE,
            kSecAttrAccount  to key
        )
        SecItemDelete(query)
        SecItemAdd(query + mapOf(kSecValueData to data), null)
    }

    private fun keychainRead(key: String): String? {
        val query = mapOf(
            kSecClass       to kSecClassGenericPassword,
            kSecAttrService to SERVICE,
            kSecAttrAccount to key,
            kSecReturnData  to true
        )
        val result = kotlin.runCatching {
            val ref = nativeHeap.allocPointerTo<kotlinx.cinterop.ObjCObject>()
            SecItemCopyMatching(query, ref.ptr)
            (ref.value as? platform.Foundation.NSData)
                ?.let { String(it.toByteArray(), Charsets.UTF_8) }
        }
        return result.getOrNull()
    }

    private fun keychainDelete(key: String) {
        val query = mapOf(
            kSecClass       to kSecClassGenericPassword,
            kSecAttrService to SERVICE,
            kSecAttrAccount to key
        )
        SecItemDelete(query)
    }

    // ── Tokens → Keychain ─────────────────────────────────────────────────────

    actual var accessToken: String?
        get() = keychainRead(KEY_ACCESS_TOKEN)
        set(v) { if (v != null) keychainWrite(KEY_ACCESS_TOKEN, v) else keychainDelete(KEY_ACCESS_TOKEN) }

    actual var idToken: String?
        get() = keychainRead(KEY_ID_TOKEN)
        set(v) { if (v != null) keychainWrite(KEY_ID_TOKEN, v) else keychainDelete(KEY_ID_TOKEN) }

    actual var refreshToken: String?
        get() = keychainRead(KEY_REFRESH_TOKEN)
        set(v) { if (v != null) keychainWrite(KEY_REFRESH_TOKEN, v) else keychainDelete(KEY_REFRESH_TOKEN) }

    actual var tokenExpiry: Long
        get() = defaults.stringForKey(KEY_TOKEN_EXPIRY)?.toLongOrNull() ?: 0L
        set(v) { defaults.setObject(v.toString(), KEY_TOKEN_EXPIRY) }

    // ── Token verification ─────────────────────────────────────────────────────

    actual var tokenVerified: Boolean
        get() = defaults.boolForKey(KEY_TOKEN_VERIFIED)
        set(v) { defaults.setBool(v, KEY_TOKEN_VERIFIED) }

    actual var tokenCode: String?
        get() = keychainRead(KEY_TOKEN_CODE)
        set(v) { if (v != null) keychainWrite(KEY_TOKEN_CODE, v) else keychainDelete(KEY_TOKEN_CODE) }

    // ── Credentials → Keychain ────────────────────────────────────────────────

    actual var savedUsername: String?
        get() = keychainRead(KEY_USERNAME)
        set(v) { if (v != null) keychainWrite(KEY_USERNAME, v) else keychainDelete(KEY_USERNAME) }

    actual var savedPassword: String?
        get() = keychainRead(KEY_PASSWORD)
        set(v) { if (v != null) keychainWrite(KEY_PASSWORD, v) else keychainDelete(KEY_PASSWORD) }

    // ── User profile → NSUserDefaults ─────────────────────────────────────────

    actual var userEmail: String?
        get() = defaults.stringForKey(KEY_USER_EMAIL)
        set(v) { if (v != null) defaults.setObject(v, KEY_USER_EMAIL) else defaults.removeObjectForKey(KEY_USER_EMAIL) }

    actual var userName: String?
        get() = defaults.stringForKey(KEY_USER_NAME)
        set(v) { if (v != null) defaults.setObject(v, KEY_USER_NAME) else defaults.removeObjectForKey(KEY_USER_NAME) }

    actual var userUid: String?
        get() = defaults.stringForKey(KEY_USER_UID)
        set(v) { if (v != null) defaults.setObject(v, KEY_USER_UID) else defaults.removeObjectForKey(KEY_USER_UID) }

    actual var accessLevel: String?
        get() = defaults.stringForKey(KEY_ACCESS_LEVEL)
        set(v) { if (v != null) defaults.setObject(v, KEY_ACCESS_LEVEL) else defaults.removeObjectForKey(KEY_ACCESS_LEVEL) }

    actual var signInAsMerchant: Boolean
        get() = defaults.boolForKey(KEY_SIGN_IN_AS_MERCHANT)
        set(v) { defaults.setBool(v, KEY_SIGN_IN_AS_MERCHANT) }

    // ── Auth details JSON → Keychain ──────────────────────────────────────────

    actual var authDetailsJson: String?
        get() = keychainRead(KEY_AUTH_DETAILS_JSON)
        set(v) { if (v != null) keychainWrite(KEY_AUTH_DETAILS_JSON, v) else keychainDelete(KEY_AUTH_DETAILS_JSON) }

    // ── Device registration → NSUserDefaults ──────────────────────────────────

    actual var deviceNumber: String?
        get() = defaults.stringForKey(KEY_DEVICE_NUMBER)
        set(v) { if (v != null) defaults.setObject(v, KEY_DEVICE_NUMBER) else defaults.removeObjectForKey(KEY_DEVICE_NUMBER) }

    actual var deviceUniqueCode: String?
        get() = defaults.stringForKey(KEY_DEVICE_UNIQUE_CODE)
        set(v) { if (v != null) defaults.setObject(v, KEY_DEVICE_UNIQUE_CODE) else defaults.removeObjectForKey(KEY_DEVICE_UNIQUE_CODE) }

    actual var dasmid: String?
        get() = defaults.stringForKey(KEY_DASMID)
        set(v) { if (v != null) defaults.setObject(v, KEY_DASMID) else defaults.removeObjectForKey(KEY_DASMID) }

    actual var merchantLegalName: String?
        get() = defaults.stringForKey(KEY_MERCHANT_LEGAL_NAME)
        set(v) { if (v != null) defaults.setObject(v, KEY_MERCHANT_LEGAL_NAME) else defaults.removeObjectForKey(KEY_MERCHANT_LEGAL_NAME) }

    // ── Device / external config JSON → NSUserDefaults ────────────────────────

    actual var deviceConfigJson: String?
        get() = defaults.stringForKey(KEY_DEVICE_CONFIG_JSON)
        set(v) { if (v != null) defaults.setObject(v, KEY_DEVICE_CONFIG_JSON) else defaults.removeObjectForKey(KEY_DEVICE_CONFIG_JSON) }

    // ── App config / URLs → NSUserDefaults ────────────────────────────────────

    actual var baseUrl: String?
        get() = defaults.stringForKey(KEY_BASE_URL)
        set(v) { if (v != null) defaults.setObject(v, KEY_BASE_URL) else defaults.removeObjectForKey(KEY_BASE_URL) }

    actual var configBaseUrl: String?
        get() = defaults.stringForKey(KEY_CONFIG_BASE_URL)
        set(v) { if (v != null) defaults.setObject(v, KEY_CONFIG_BASE_URL) else defaults.removeObjectForKey(KEY_CONFIG_BASE_URL) }

    actual var transactionDetailsUrl: String?
        get() = defaults.stringForKey(KEY_TRANSACTION_DETAILS_URL)
        set(v) { if (v != null) defaults.setObject(v, KEY_TRANSACTION_DETAILS_URL) else defaults.removeObjectForKey(KEY_TRANSACTION_DETAILS_URL) }

    // ── FCM → NSUserDefaults ──────────────────────────────────────────────────

    actual var fcmToken: String?
        get() = defaults.stringForKey(KEY_FCM_TOKEN)
        set(v) { if (v != null) defaults.setObject(v, KEY_FCM_TOKEN) else defaults.removeObjectForKey(KEY_FCM_TOKEN) }

    // ── Cart → NSUserDefaults ─────────────────────────────────────────────────

    actual var cartJson: String?
        get() = defaults.stringForKey(KEY_CART_JSON)
        set(v) { if (v != null) defaults.setObject(v, KEY_CART_JSON) else defaults.removeObjectForKey(KEY_CART_JSON) }

    // ── Payment methods → NSUserDefaults ──────────────────────────────────────

    actual var paymentMethodsJson: String?
        get() = defaults.stringForKey(KEY_PAYMENT_METHODS_JSON)
        set(v) { if (v != null) defaults.setObject(v, KEY_PAYMENT_METHODS_JSON) else defaults.removeObjectForKey(KEY_PAYMENT_METHODS_JSON) }

    actual var externalConfigJson: String?
        get() = defaults.stringForKey(KEY_EXTERNAL_CONFIG_JSON)
        set(v) { if (v != null) defaults.setObject(v, KEY_EXTERNAL_CONFIG_JSON) else defaults.removeObjectForKey(KEY_EXTERNAL_CONFIG_JSON) }

    // ── Geo restriction → NSUserDefaults ──────────────────────────────────────

    actual var merchantCountriesJson: String?
        get() = defaults.stringForKey(KEY_MERCHANT_COUNTRIES_JSON)
        set(v) { if (v != null) defaults.setObject(v, KEY_MERCHANT_COUNTRIES_JSON) else defaults.removeObjectForKey(KEY_MERCHANT_COUNTRIES_JSON) }

    // ── Biometric → NSUserDefaults ────────────────────────────────────────────

    actual var isBiometricEnabled: Boolean
        get() = defaults.boolForKey(KEY_BIOMETRIC_ENABLED)
        set(v) { defaults.setBool(v, KEY_BIOMETRIC_ENABLED) }

    // ── Generic helpers ───────────────────────────────────────────────────────

    actual fun getString(key: String): String? = defaults.stringForKey(key)
    actual fun putString(key: String, value: String) { defaults.setObject(value, key) }
    actual fun getBoolean(key: String, default: Boolean): Boolean =
        if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else default
    actual fun putBoolean(key: String, value: Boolean) { defaults.setBool(value, key) }

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
        defaults.dictionaryRepresentation().keys.forEach {
            defaults.removeObjectForKey(it as String)
        }
        listOf(
            KEY_ACCESS_TOKEN, KEY_ID_TOKEN, KEY_REFRESH_TOKEN,
            KEY_TOKEN_CODE, KEY_USERNAME, KEY_PASSWORD, KEY_AUTH_DETAILS_JSON
        ).forEach { keychainDelete(it) }
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
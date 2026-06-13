package com.paymentoptions.pos.storage

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativePtr
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf

import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar

import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.NSNumber
import platform.Foundation.setObject
import platform.Foundation.NSCopyingProtocol

import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess

import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

import platform.posix.memcpy
import com.russhwolf.settings.Settings

actual fun AppStorage(): StorageInterface {
    return IosAppStorage
}
@OptIn(ExperimentalForeignApi::class)
object IosAppStorage : StorageInterface {

    private lateinit var settings: Settings
    private val defaults get() = NSUserDefaults.standardUserDefaults
    private const val SERVICE = "com.paymentoptions.pos"

    override fun init(settings: Settings) {
        this.settings = settings
    }

    // -------------------------------------------------------------------------
    // NSData Helpers
    // -------------------------------------------------------------------------

    private fun ByteArray.toNSData(): NSData =
        usePinned {
            NSData.create(
                bytes = it.addressOf(0),
                length = size.toULong()
            )
        }

    private fun NSData.toByteArray(): ByteArray {
        val byteArray = ByteArray(length.toInt())

        byteArray.usePinned {
            memcpy(
                it.addressOf(0),
                bytes,
                length
            )
        }

        return byteArray
    }

    private fun CPointer<*>?.asNSCopying(): NSCopyingProtocol = interpretObjCPointer<NSCopyingProtocol>(this!!.rawValue)

    private fun keychainQuery(key: String): NSMutableDictionary {
        val dict = NSMutableDictionary()

        dict.setObject(kSecClassGenericPassword, kSecClass.asNSCopying())
        dict.setObject(SERVICE, kSecAttrService.asNSCopying())
        dict.setObject(key, kSecAttrAccount.asNSCopying())

        return dict
    }

    // -------------------------------------------------------------------------
    // Keychain Helpers
    // -------------------------------------------------------------------------

    private fun keychainWrite(key: String, value: String) {
        keychainDelete(key)

        val data = value.encodeToByteArray().toNSData()

        val query = keychainQuery(key).apply {
            setObject(data, kSecValueData.asNSCopying())
        }

        SecItemAdd(query as CFDictionaryRef, null)
    }

    private fun keychainRead(key: String): String? = memScoped {
        val query = keychainQuery(key).apply {
            setObject(NSNumber(bool = true), kSecReturnData.asNSCopying())
            setObject(kSecMatchLimitOne, kSecMatchLimit.asNSCopying())
        }

        val result = alloc<CFTypeRefVar>()

        val status = SecItemCopyMatching(
            query as CFDictionaryRef,
            result.ptr
        )

        if (status != errSecSuccess) {
            return null
        }

        val data = result.ptr.pointed.value as? NSData ?: return null

        data.toByteArray().decodeToString()
    }

    private fun keychainDelete(key: String) {
        val query = keychainQuery(key)
        SecItemDelete(query as CFDictionaryRef)
    }

    // -------------------------------------------------------------------------
    // Sensitive Data (Keychain)
    // -------------------------------------------------------------------------

    override var accessToken: String?
        get() = keychainRead(KEY_ACCESS_TOKEN)
        set(value) {
            if (value == null) keychainDelete(KEY_ACCESS_TOKEN)
            else keychainWrite(KEY_ACCESS_TOKEN, value)
        }

    override var idToken: String?
        get() = keychainRead(KEY_ID_TOKEN)
        set(value) {
            if (value == null) keychainDelete(KEY_ID_TOKEN)
            else keychainWrite(KEY_ID_TOKEN, value)
        }

    override var refreshToken: String?
        get() = keychainRead(KEY_REFRESH_TOKEN)
        set(value) {
            if (value == null) keychainDelete(KEY_REFRESH_TOKEN)
            else keychainWrite(KEY_REFRESH_TOKEN, value)
        }

    override var tokenCode: String?
        get() = keychainRead(KEY_TOKEN_CODE)
        set(value) {
            if (value == null) keychainDelete(KEY_TOKEN_CODE)
            else keychainWrite(KEY_TOKEN_CODE, value)
        }

    override var savedUsername: String?
        get() = keychainRead(KEY_USERNAME)
        set(value) {
            if (value == null) keychainDelete(KEY_USERNAME)
            else keychainWrite(KEY_USERNAME, value)
        }

    override var savedPassword: String?
        get() = keychainRead(KEY_PASSWORD)
        set(value) {
            if (value == null) keychainDelete(KEY_PASSWORD)
            else keychainWrite(KEY_PASSWORD, value)
        }

    override var authDetailsJson: String?
        get() = keychainRead(KEY_AUTH_DETAILS_JSON)
        set(value) {
            if (value == null) keychainDelete(KEY_AUTH_DETAILS_JSON)
            else keychainWrite(KEY_AUTH_DETAILS_JSON, value)
        }

    // -------------------------------------------------------------------------
    // NSUserDefaults
    // -------------------------------------------------------------------------

    override var tokenExpiry: Long
        get() = defaults.stringForKey(KEY_TOKEN_EXPIRY)?.toLongOrNull() ?: 0L
        set(value) = defaults.setObject(value.toString(), KEY_TOKEN_EXPIRY)

    override var tokenVerified: Boolean
        get() = defaults.boolForKey(KEY_TOKEN_VERIFIED)
        set(value) = defaults.setBool(value, KEY_TOKEN_VERIFIED)

    override var userEmail: String?
        get() = defaults.stringForKey(KEY_USER_EMAIL)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_USER_EMAIL)
            else
                defaults.setObject(value, KEY_USER_EMAIL)
        }

    override var userName: String?
        get() = defaults.stringForKey(KEY_USER_NAME)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_USER_NAME)
            else
                defaults.setObject(value, KEY_USER_NAME)
        }

    override var userUid: String?
        get() = defaults.stringForKey(KEY_USER_UID)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_USER_UID)
            else
                defaults.setObject(value, KEY_USER_UID)
        }

    override var accessLevel: String?
        get() = defaults.stringForKey(KEY_ACCESS_LEVEL)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_ACCESS_LEVEL)
            else
                defaults.setObject(value, KEY_ACCESS_LEVEL)
        }

    override var signInAsMerchant: Boolean
        get() = defaults.boolForKey(KEY_SIGN_IN_AS_MERCHANT)
        set(value) = defaults.setBool(value, KEY_SIGN_IN_AS_MERCHANT)

    override var deviceNumber: String?
        get() = defaults.stringForKey(KEY_DEVICE_NUMBER)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_DEVICE_NUMBER)
            else
                defaults.setObject(value, KEY_DEVICE_NUMBER)
        }

    override var deviceUniqueCode: String?
        get() = defaults.stringForKey(KEY_DEVICE_UNIQUE_CODE)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_DEVICE_UNIQUE_CODE)
            else
                defaults.setObject(value, KEY_DEVICE_UNIQUE_CODE)
        }

    override var dasmid: String?
        get() = defaults.stringForKey(KEY_DASMID)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_DASMID)
            else
                defaults.setObject(value, KEY_DASMID)
        }

    override var merchantLegalName: String?
        get() = defaults.stringForKey(KEY_MERCHANT_LEGAL_NAME)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_MERCHANT_LEGAL_NAME)
            else
                defaults.setObject(value, KEY_MERCHANT_LEGAL_NAME)
        }

    override var deviceConfigJson: String?
        get() = defaults.stringForKey(KEY_DEVICE_CONFIG_JSON)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_DEVICE_CONFIG_JSON)
            else
                defaults.setObject(value, KEY_DEVICE_CONFIG_JSON)
        }

    override var baseUrl: String?
        get() = defaults.stringForKey(KEY_BASE_URL)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_BASE_URL)
            else
                defaults.setObject(value, KEY_BASE_URL)
        }

    override var configBaseUrl: String?
        get() = defaults.stringForKey(KEY_CONFIG_BASE_URL)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_CONFIG_BASE_URL)
            else
                defaults.setObject(value, KEY_CONFIG_BASE_URL)
        }

    override var transactionDetailsUrl: String?
        get() = defaults.stringForKey(KEY_TRANSACTION_DETAILS_URL)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_TRANSACTION_DETAILS_URL)
            else
                defaults.setObject(value, KEY_TRANSACTION_DETAILS_URL)
        }

    override var fcmToken: String?
        get() = defaults.stringForKey(KEY_FCM_TOKEN)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_FCM_TOKEN)
            else
                defaults.setObject(value, KEY_FCM_TOKEN)
        }

    override var cartJson: String?
        get() = defaults.stringForKey(KEY_CART_JSON)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_CART_JSON)
            else
                defaults.setObject(value, KEY_CART_JSON)
        }

    override var paymentMethodsJson: String?
        get() = defaults.stringForKey(KEY_PAYMENT_METHODS_JSON)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_PAYMENT_METHODS_JSON)
            else
                defaults.setObject(value, KEY_PAYMENT_METHODS_JSON)
        }

    override var externalConfigJson: String?
        get() = defaults.stringForKey(KEY_EXTERNAL_CONFIG_JSON)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_EXTERNAL_CONFIG_JSON)
            else
                defaults.setObject(value, KEY_EXTERNAL_CONFIG_JSON)
        }

    override var merchantCountriesJson: String?
        get() = defaults.stringForKey(KEY_MERCHANT_COUNTRIES_JSON)
        set(value) {
            if (value == null)
                defaults.removeObjectForKey(KEY_MERCHANT_COUNTRIES_JSON)
            else
                defaults.setObject(value, KEY_MERCHANT_COUNTRIES_JSON)
        }

    override var isBiometricEnabled: Boolean
        get() = defaults.boolForKey(KEY_BIOMETRIC_ENABLED)
        set(value) = defaults.setBool(value, KEY_BIOMETRIC_ENABLED)

    // -------------------------------------------------------------------------
    // Generic Helpers
    // -------------------------------------------------------------------------

    override fun getString(key: String): String? =
        defaults.stringForKey(key)

    override fun putString(key: String, value: String) =
        defaults.setObject(value, key)

    override fun getBoolean(key: String, default: Boolean): Boolean =
        if (defaults.objectForKey(key) != null)
            defaults.boolForKey(key)
        else
            default

    override fun putBoolean(key: String, value: Boolean) =
        defaults.setBool(value, key)

    override fun getLong(key: String, default: Long): Long =
        defaults.stringForKey(key)?.toLongOrNull() ?: default

    override fun putLong(key: String, value: Long) =
        defaults.setObject(value.toString(), key)

    override fun isLoggedIn(): Boolean =
        !accessToken.isNullOrBlank()

    override fun clearAuthData() {
        accessToken = null
        idToken = null
        refreshToken = null
        authDetailsJson = null
        userEmail = null
        userName = null
        userUid = null
        accessLevel = null
    }

    override fun clearAll() {
        defaults.dictionaryRepresentation().keys.forEach {
            defaults.removeObjectForKey(it as String)
        }

        listOf(
            KEY_ACCESS_TOKEN,
            KEY_ID_TOKEN,
            KEY_REFRESH_TOKEN,
            KEY_TOKEN_CODE,
            KEY_USERNAME,
            KEY_PASSWORD,
            KEY_AUTH_DETAILS_JSON
        ).forEach(::keychainDelete)
    }

    // -------------------------------------------------------------------------
    // Keys
    // -------------------------------------------------------------------------

    private const val KEY_ACCESS_TOKEN = "accessToken"
    private const val KEY_ID_TOKEN = "idToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val KEY_TOKEN_EXPIRY = "tokenExpiry"
    private const val KEY_TOKEN_VERIFIED = "tokenVerified"
    private const val KEY_TOKEN_CODE = "tokenCode"
    private const val KEY_USERNAME = "savedUsername"
    private const val KEY_PASSWORD = "savedPassword"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_UID = "userUid"
    private const val KEY_ACCESS_LEVEL = "accessLevel"
    private const val KEY_SIGN_IN_AS_MERCHANT = "signInAsMerchant"
    private const val KEY_AUTH_DETAILS_JSON = "authDetailsJson"
    private const val KEY_DEVICE_NUMBER = "deviceNumber"
    private const val KEY_DEVICE_UNIQUE_CODE = "deviceUniqueCode"
    private const val KEY_DASMID = "dasmid"
    private const val KEY_MERCHANT_LEGAL_NAME = "merchantLegalName"
    private const val KEY_DEVICE_CONFIG_JSON = "deviceConfigJson"
    private const val KEY_BASE_URL = "baseUrl"
    private const val KEY_CONFIG_BASE_URL = "configBaseUrl"
    private const val KEY_TRANSACTION_DETAILS_URL = "transactionDetailsUrl"
    private const val KEY_FCM_TOKEN = "fcmToken"
    private const val KEY_CART_JSON = "cartJson"
    private const val KEY_PAYMENT_METHODS_JSON = "paymentMethodsJson"
    private const val KEY_EXTERNAL_CONFIG_JSON = "externalConfigJson"
    private const val KEY_MERCHANT_COUNTRIES_JSON = "merchantCountriesJson"
    private const val KEY_BIOMETRIC_ENABLED = "biometricEnabled"
}


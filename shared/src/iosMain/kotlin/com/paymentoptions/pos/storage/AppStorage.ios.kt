package com.paymentoptions.pos.storage

import com.russhwolf.settings.Settings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*
import platform.posix.memcpy

// ── Logger ────────────────────────────────────────────────────────────────────

private object StorageLogger {
    private const val TAG = "[AppStorage]"

    fun d(message: String) {
        println("$TAG DEBUG | $message")
    }

    fun e(message: String, status: Int? = null) {
        val statusInfo = status?.let { " | OSStatus=$it (${osStatusDescription(it)})" } ?: ""
        println("$TAG ERROR | $message$statusInfo")
    }

    fun w(message: String) {
        println("$TAG WARN  | $message")
    }

    private fun osStatusDescription(status: Int): String = when (status) {
        0        -> "errSecSuccess"
        -4       -> "errSecUnimplemented"
        -50      -> "errSecParam — bad/mistyped parameter (CF constant not bridged to NS type?)"
        -25291   -> "errSecNotAvailable"
        -25292   -> "errSecReadOnly"
        -25293   -> "errSecAuthFailed"
        -25294   -> "errSecNoSuchKeychain"
        -25295   -> "errSecInvalidKeychain"
        -25296   -> "errSecDuplicateKeychain"
        -25299   -> "errSecDuplicateItem"
        -25300   -> "errSecItemNotFound"
        -25308   -> "errSecInteractionNotAllowed"
        -25311   -> "errSecDataNotAvailable"
        -25312   -> "errSecDataNotModifiable"
        -34018   -> "errSecMissingEntitlement — check .entitlements / Keychain Sharing capability"
        else     -> "unknown"
    }
}

// ── Actual storage ────────────────────────────────────────────────────────────

actual val AppStorage: StorageInterface = IosAppStorage

@OptIn(ExperimentalForeignApi::class)
private object IosAppStorage : StorageInterface {

    private var settings: Settings? = null
    private val defaults = NSUserDefaults.standardUserDefaults
    private const val SERVICE = "com.paymentoptions.pos"

    override fun init(settings: Settings) {
        this.settings = settings
        StorageLogger.d("init() called — storage ready")
    }

    // ── NSData helpers ────────────────────────────────────────────────────────

    private fun ByteArray.toNSData(): NSData = usePinned {
        NSData.create(bytes = it.addressOf(0), length = size.toULong())
    }

    private fun NSData.toByteArray(): ByteArray {
        val byteArray = ByteArray(length.toInt())
        if (length > 0u) {
            byteArray.usePinned { memcpy(it.addressOf(0), bytes, length) }
        }
        return byteArray
    }

    // ── Keychain query builder ────────────────────────────────────────────────
    //
    // ROOT CAUSE FIX — errSecParam = -50:
    // kSec* constants are CFStringRef on the C side. When inserted directly into
    // NSMutableDictionary without bridging, Security framework receives a raw
    // CFTypeRef instead of an NSString and immediately returns -50 (bad param).
    //
    // Fix: CFBridgingRelease() transfers ownership from CF to ARC and returns
    // the value as a proper Objective-C object (NSString/NSData/etc.) that
    // NSMutableDictionary and the Security framework accept correctly.

    private fun cfStr(ref: CFTypeRef?): Any =
        ref ?: error("Null CFTypeRef")
    private fun buildKeychainQuery(key: String): NSMutableDictionary =
        NSMutableDictionary().apply {
            // ✅ Bridge CF constants → NSString before inserting as values
            setObject(cfStr(kSecClassGenericPassword), forKey = cfStr(kSecClass) as NSCopyingProtocol)
            setObject(SERVICE,                          forKey = cfStr(kSecAttrService) as NSCopyingProtocol)
            setObject(key,                              forKey = cfStr(kSecAttrAccount) as NSCopyingProtocol)
        }

    // ── Keychain WRITE ────────────────────────────────────────────────────────

    private fun keychainWrite(key: String, value: String) {
        StorageLogger.d("keychainWrite() → key=$key length=${value.length}")

        keychainDelete(key) // delete first to avoid errSecDuplicateItem

        val data = value.encodeToByteArray().toNSData()

        val query = buildKeychainQuery(key).apply {
            // ✅ Bridge kSecValueData CF constant → NSString key
            setObject(data, forKey = cfStr(kSecValueData) as NSCopyingProtocol)
        }

        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef
        val status: Int
        try {
            status = SecItemAdd(cfQuery, null)
        } finally {
            CFRelease(cfQuery)
        }

        if (status == errSecSuccess) {
            StorageLogger.d("keychainWrite() ✅ SUCCESS → key=$key")
        } else {
            StorageLogger.e("keychainWrite() ❌ FAILED → key=$key", status)
        }
    }

    // ── Keychain READ ─────────────────────────────────────────────────────────

    private fun keychainRead(key: String): String? = memScoped {
        StorageLogger.d("keychainRead() → key=$key")

        val query = buildKeychainQuery(key).apply {
            // ✅ NSNumber(bool=true) instead of bare Kotlin `true` (doesn't auto-box in K/N)
            // ✅ Bridge kSecReturnData and kSecMatchLimit CF constants → NSString keys
            // ✅ Bridge kSecMatchLimitOne CF constant → NSString value
            setObject(
                NSNumber.numberWithBool(true),
                forKey = cfStr(kSecReturnData) as NSCopyingProtocol
            )
            setObject(
                kSecMatchLimitOne,
                forKey = kSecMatchLimit as NSCopyingProtocol
            )
            //setObject(cfStr(     kSecMatchLimitOne),    forKey = cfStr(kSecMatchLimit) as NSCopyingProtocol)
        }

        val result = alloc<CFTypeRefVar>()

        StorageLogger.d("Keychain query = $query")

        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef
        val status = try {
            SecItemCopyMatching(cfQuery, result.ptr)
        } finally {
            CFRelease(cfQuery)
        }

        if (status != errSecSuccess) {
            if (status == -25300 /* errSecItemNotFound */) {
                StorageLogger.d("keychainRead() → key=$key not found (item does not exist yet)")
            } else {
                StorageLogger.e("keychainRead() ❌ FAILED → key=$key", status)
            }
            return null
        }

        val data = CFBridgingRelease(result.value) as? NSData
        if (data == null) {
            StorageLogger.e("keychainRead() ❌ CFBridgingRelease returned null or wrong type → key=$key")
            return null
        }

        val decoded = data.toByteArray().decodeToString()
        StorageLogger.d("keychainRead() ✅ SUCCESS → key=$key length=${decoded.length}")
        decoded
    }

    // ── Keychain DELETE ───────────────────────────────────────────────────────

    private fun keychainDelete(key: String) {
        StorageLogger.d("keychainDelete() → key=$key")

        val query = buildKeychainQuery(key)
        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef
        val status = try {
            SecItemDelete(cfQuery)
        } finally {
            CFRelease(cfQuery)
        }

        when (status) {
            errSecSuccess -> StorageLogger.d("keychainDelete() ✅ deleted → key=$key")
            -25300        -> StorageLogger.d("keychainDelete() → key=$key did not exist, nothing to delete")
            else          -> StorageLogger.e("keychainDelete() ❌ FAILED → key=$key", status)
        }
    }

    // ── NSUserDefaults helpers ────────────────────────────────────────────────

    private fun defaultsWrite(key: String, value: String?) {
        if (value != null) {
            defaults.setObject(value, key)
            val verify = defaults.stringForKey(key)
            if (verify == value) {
                StorageLogger.d("defaultsWrite() ✅ key=$key")
            } else {
                StorageLogger.e("defaultsWrite() ❌ verify mismatch → key=$key written='$value' read='$verify'")
            }
        } else {
            defaults.removeObjectForKey(key)
            StorageLogger.d("defaultsWrite() removed key=$key")
        }
    }

    private fun defaultsRead(key: String): String? {
        val value = defaults.stringForKey(key)
        StorageLogger.d("defaultsRead() → key=$key found=${value != null}")
        return value
    }

    private fun defaultsWriteBool(key: String, value: Boolean) {
        defaults.setBool(value, key)
        StorageLogger.d("defaultsWriteBool() → key=$key value=$value")
    }

    private fun defaultsReadBool(key: String): Boolean {
        val value = defaults.boolForKey(key)
        StorageLogger.d("defaultsReadBool() → key=$key value=$value")
        return value
    }

    // ── Tokens (Keychain) ─────────────────────────────────────────────────────

    override var accessToken: String?
        get() = keychainRead(KEY_ACCESS_TOKEN)
        set(v) { if (v != null) keychainWrite(KEY_ACCESS_TOKEN, v) else keychainDelete(KEY_ACCESS_TOKEN) }

    override var idToken: String?
        get() = keychainRead(KEY_ID_TOKEN)
        set(v) { if (v != null) keychainWrite(KEY_ID_TOKEN, v) else keychainDelete(KEY_ID_TOKEN) }

    override var refreshToken: String?
        get() = keychainRead(KEY_REFRESH_TOKEN)
        set(v) { if (v != null) keychainWrite(KEY_REFRESH_TOKEN, v) else keychainDelete(KEY_REFRESH_TOKEN) }

    override var tokenExpiry: Long
        get() {
            val v = defaultsRead(KEY_TOKEN_EXPIRY)?.toLongOrNull() ?: 0L
            StorageLogger.d("get tokenExpiry=$v")
            return v
        }
        set(v) {
            StorageLogger.d("set tokenExpiry=$v")
            defaultsWrite(KEY_TOKEN_EXPIRY, v.toString())
        }

    // ── Token verification ────────────────────────────────────────────────────

    override var tokenVerified: Boolean
        get() = defaultsReadBool(KEY_TOKEN_VERIFIED)
        set(v) { defaultsWriteBool(KEY_TOKEN_VERIFIED, v) }

    override var tokenCode: String?
        get() = keychainRead(KEY_TOKEN_CODE)
        set(v) { if (v != null) keychainWrite(KEY_TOKEN_CODE, v) else keychainDelete(KEY_TOKEN_CODE) }

    // ── Credentials (Keychain) ────────────────────────────────────────────────

    override var savedUsername: String?
        get() = keychainRead(KEY_USERNAME)
        set(v) { if (v != null) keychainWrite(KEY_USERNAME, v) else keychainDelete(KEY_USERNAME) }

    override var savedPassword: String?
        get() = keychainRead(KEY_PASSWORD)
        set(v) { if (v != null) keychainWrite(KEY_PASSWORD, v) else keychainDelete(KEY_PASSWORD) }

    // ── User profile (NSUserDefaults) ─────────────────────────────────────────

    override var userEmail: String?
        get() = defaultsRead(KEY_USER_EMAIL)
        set(v) { defaultsWrite(KEY_USER_EMAIL, v) }

    override var userName: String?
        get() = defaultsRead(KEY_USER_NAME)
        set(v) { defaultsWrite(KEY_USER_NAME, v) }

    override var userUid: String?
        get() = defaultsRead(KEY_USER_UID)
        set(v) { defaultsWrite(KEY_USER_UID, v) }

    override var accessLevel: String?
        get() = defaultsRead(KEY_ACCESS_LEVEL)
        set(v) { defaultsWrite(KEY_ACCESS_LEVEL, v) }

    override var signInAsMerchant: Boolean
        get() = defaultsReadBool(KEY_SIGN_IN_AS_MERCHANT)
        set(v) { defaultsWriteBool(KEY_SIGN_IN_AS_MERCHANT, v) }

    // ── Auth details JSON (Keychain) ──────────────────────────────────────────

    override var authDetailsJson: String?
        get() = keychainRead(KEY_AUTH_DETAILS_JSON)
        set(v) { if (v != null) keychainWrite(KEY_AUTH_DETAILS_JSON, v) else keychainDelete(KEY_AUTH_DETAILS_JSON) }

    // ── Device registration (NSUserDefaults) ──────────────────────────────────

    override var deviceNumber: String?
        get() = defaultsRead(KEY_DEVICE_NUMBER)
        set(v) { defaultsWrite(KEY_DEVICE_NUMBER, v) }

    override var deviceUniqueCode: String?
        get() = defaultsRead(KEY_DEVICE_UNIQUE_CODE)
        set(v) { defaultsWrite(KEY_DEVICE_UNIQUE_CODE, v) }

    override var dasmid: String?
        get() = defaultsRead(KEY_DASMID)
        set(v) { defaultsWrite(KEY_DASMID, v) }

    override var merchantLegalName: String?
        get() = defaultsRead(KEY_MERCHANT_LEGAL_NAME)
        set(v) { defaultsWrite(KEY_MERCHANT_LEGAL_NAME, v) }

    // ── Device / external config JSON (NSUserDefaults) ────────────────────────

    override var deviceConfigJson: String?
        get() = defaultsRead(KEY_DEVICE_CONFIG_JSON)
        set(v) { defaultsWrite(KEY_DEVICE_CONFIG_JSON, v) }

    // ── App config / URLs (NSUserDefaults) ────────────────────────────────────

    override var baseUrl: String?
        get() = defaultsRead(KEY_BASE_URL)
        set(v) { defaultsWrite(KEY_BASE_URL, v) }

    override var configBaseUrl: String?
        get() = defaultsRead(KEY_CONFIG_BASE_URL)
        set(v) { defaultsWrite(KEY_CONFIG_BASE_URL, v) }

    override var transactionDetailsUrl: String?
        get() = defaultsRead(KEY_TRANSACTION_DETAILS_URL)
        set(v) { defaultsWrite(KEY_TRANSACTION_DETAILS_URL, v) }

    // ── FCM (NSUserDefaults) ──────────────────────────────────────────────────

    override var fcmToken: String?
        get() = defaultsRead(KEY_FCM_TOKEN)
        set(v) { defaultsWrite(KEY_FCM_TOKEN, v) }

    // ── Cart (NSUserDefaults) ─────────────────────────────────────────────────

    override var cartJson: String?
        get() = defaultsRead(KEY_CART_JSON)
        set(v) { defaultsWrite(KEY_CART_JSON, v) }

    // ── Payment methods (NSUserDefaults) ──────────────────────────────────────

    override var paymentMethodsJson: String?
        get() = defaultsRead(KEY_PAYMENT_METHODS_JSON)
        set(v) { defaultsWrite(KEY_PAYMENT_METHODS_JSON, v) }

    override var externalConfigJson: String?
        get() = defaultsRead(KEY_EXTERNAL_CONFIG_JSON)
        set(v) { defaultsWrite(KEY_EXTERNAL_CONFIG_JSON, v) }

    // ── Geo restriction (NSUserDefaults) ──────────────────────────────────────

    override var merchantCountriesJson: String?
        get() = defaultsRead(KEY_MERCHANT_COUNTRIES_JSON)
        set(v) { defaultsWrite(KEY_MERCHANT_COUNTRIES_JSON, v) }

    // ── Biometric (NSUserDefaults) ────────────────────────────────────────────

    override var isBiometricEnabled: Boolean
        get() = defaultsReadBool(KEY_BIOMETRIC_ENABLED)
        set(v) { defaultsWriteBool(KEY_BIOMETRIC_ENABLED, v) }

    // ── Generic Helpers ───────────────────────────────────────────────────────

    override fun getString(key: String): String? {
        val v = defaults.stringForKey(key)
        StorageLogger.d("getString() → key=$key found=${v != null}")
        return v
    }

    override fun putString(key: String, value: String) {
        StorageLogger.d("putString() → key=$key")
        defaults.setObject(value, key)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        val v = if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else default
        StorageLogger.d("getBoolean() → key=$key value=$v")
        return v
    }

    override fun putBoolean(key: String, value: Boolean) {
        StorageLogger.d("putBoolean() → key=$key value=$value")
        defaults.setBool(value, key)
    }

    // ── Session helpers ───────────────────────────────────────────────────────

    override fun isLoggedIn(): Boolean {
        val loggedIn = !accessToken.isNullOrBlank()
        StorageLogger.d("isLoggedIn() → $loggedIn")
        return loggedIn
    }

    override fun clearAuthData() {
        StorageLogger.w("clearAuthData() called — wiping auth tokens & profile")
        accessToken     = null
        idToken         = null
        refreshToken    = null
        tokenExpiry     = 0L
        userEmail       = null
        userName        = null
        userUid         = null
        accessLevel     = null
        authDetailsJson = null
        StorageLogger.d("clearAuthData() ✅ done")
    }

    override fun clearAll() {
        StorageLogger.w("clearAll() called — wiping ALL storage")

        defaults.dictionaryRepresentation().keys.forEach {
            defaults.removeObjectForKey(it as String)
        }

        listOf(
            KEY_ACCESS_TOKEN, KEY_ID_TOKEN, KEY_REFRESH_TOKEN,
            KEY_TOKEN_CODE, KEY_USERNAME, KEY_PASSWORD, KEY_AUTH_DETAILS_JSON
        ).forEach { keychainDelete(it) }

        StorageLogger.d("clearAll() ✅ done")
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


//package com.paymentoptions.pos.storage
//
//import com.russhwolf.settings.Settings
//import kotlinx.cinterop.ExperimentalForeignApi
//import kotlinx.cinterop.alloc
//import kotlinx.cinterop.addressOf
//import kotlinx.cinterop.memScoped
//import kotlinx.cinterop.ptr
//import kotlinx.cinterop.usePinned
//import kotlinx.cinterop.value
//import platform.CoreFoundation.CFDictionaryRef
//import platform.CoreFoundation.CFTypeRefVar
//import platform.Foundation.NSCopyingProtocol
//import platform.Foundation.NSData
//import platform.Foundation.NSMutableDictionary
//import platform.Foundation.NSNumber
//import platform.Foundation.NSUserDefaults
//import platform.Foundation.create
//import platform.Security.SecItemAdd
//import platform.Security.SecItemCopyMatching
//import platform.Security.SecItemDelete
//import platform.Security.errSecSuccess
//import platform.Security.kSecAttrAccount
//import platform.Security.kSecAttrService
//import platform.Security.kSecClass
//import platform.Security.kSecClassGenericPassword
//import platform.Security.kSecMatchLimit
//import platform.Security.kSecMatchLimitOne
//import platform.Security.kSecReturnData
//import platform.Security.kSecValueData
//import platform.posix.memcpy
//import kotlinx.cinterop.reinterpret
//import kotlinx.cinterop.*
//import platform.CoreFoundation.*
//import platform.Foundation.*
//import platform.Security.*
//
//actual val AppStorage: StorageInterface = IosAppStorage
//
//@OptIn(ExperimentalForeignApi::class)
//private object IosAppStorage : StorageInterface {
//
//    private var settings: Settings? = null
//    private val defaults = NSUserDefaults.standardUserDefaults
//    private const val SERVICE = "com.paymentoptions.pos"
//
//    override fun init(settings: Settings) {
//        this.settings = settings
//    }
//
//    // ── Helpers ───────────────────────────────────────────────────────────────
//
//    private fun ByteArray.toNSData(): NSData = usePinned {
//        NSData.create(bytes = it.addressOf(0), length = size.toULong())
//    }
//
//    private fun NSData.toByteArray(): ByteArray {
//        val byteArray = ByteArray(length.toInt())
//        if (length > 0u) {
//            byteArray.usePinned { memcpy(it.addressOf(0), bytes, length) }
//        }
//        return byteArray
//    }
//
//    // ✅ Key fix: cast to NSCopying, which is what setObject(forKey:) expects
//    private fun buildKeychainQuery(key: String): NSMutableDictionary =
//        NSMutableDictionary().apply {
//            setObject(kSecClassGenericPassword, forKey = kSecClass as NSCopyingProtocol)
//            setObject(SERVICE,                  forKey = kSecAttrService as NSCopyingProtocol)
//            setObject(key,                      forKey = kSecAttrAccount as NSCopyingProtocol)
//        }
//
//
// // **********
// private fun keychainWrite(key: String, value: String) {
//     keychainDelete(key)
//
//     val data = value.encodeToByteArray().toNSData()
//
//     val query = buildKeychainQuery(key).apply {
//         setObject(data, forKey = kSecValueData as NSCopyingProtocol)
//     }
//
//     // 1. Explicitly bridge the NSDictionary to a CFDictionaryRef
//     val cfQuery = CFBridgingRetain(query) as CFDictionaryRef
//
//     try {
//         SecItemAdd(cfQuery, null)
//     } finally {
//         // 2. Prevent memory leaks by releasing the Core Foundation object
//         CFRelease(cfQuery)
//     }
// }
//
//    private fun keychainRead(key: String): String? = memScoped {
//        val query = buildKeychainQuery(key).apply {
//            setObject(true, forKey = kSecReturnData as NSCopyingProtocol)
//            setObject(kSecMatchLimitOne, forKey = kSecMatchLimit as NSCopyingProtocol)
//        }
//
//        val result = alloc<CFTypeRefVar>()
//
//        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef
//        val status = try {
//            SecItemCopyMatching(cfQuery, result.ptr)
//        } finally {
//            CFRelease(cfQuery)
//        }
//
//        if (status != errSecSuccess) return null
//
//        // 3. Bridge the returned CFTypeRef (CPointer) back to an Objective-C NSData object
//        val data = CFBridgingRelease(result.value) as? NSData ?: return null
//
//        data.toByteArray().decodeToString()
//    }
//
//    private fun keychainDelete(key: String) {
//        val query = buildKeychainQuery(key)
//
//        val cfQuery = CFBridgingRetain(query) as CFDictionaryRef
//        try {
//            SecItemDelete(cfQuery)
//        } finally {
//            CFRelease(cfQuery)
//        }
//    }
// // ***********
//
//    // ── Tokens (Keychain) ─────────────────────────────────────────────────────
//
//    override var accessToken: String?
//        get() = keychainRead(KEY_ACCESS_TOKEN)
//        set(v) { if (v != null) keychainWrite(KEY_ACCESS_TOKEN, v) else keychainDelete(KEY_ACCESS_TOKEN) }
//
//    override var idToken: String?
//        get() = keychainRead(KEY_ID_TOKEN)
//        set(v) { if (v != null) keychainWrite(KEY_ID_TOKEN, v) else keychainDelete(KEY_ID_TOKEN) }
//
//    override var refreshToken: String?
//        get() = keychainRead(KEY_REFRESH_TOKEN)
//        set(v) { if (v != null) keychainWrite(KEY_REFRESH_TOKEN, v) else keychainDelete(KEY_REFRESH_TOKEN) }
//
//    override var tokenExpiry: Long
//        get() = defaults.stringForKey(KEY_TOKEN_EXPIRY)?.toLongOrNull() ?: 0L
//        set(v) { defaults.setObject(v.toString(), KEY_TOKEN_EXPIRY) }
//
//    // ── Token verification ────────────────────────────────────────────────────
//
//    override var tokenVerified: Boolean
//        get() = defaults.boolForKey(KEY_TOKEN_VERIFIED)
//        set(v) { defaults.setBool(v, KEY_TOKEN_VERIFIED) }
//
//    override var tokenCode: String?
//        get() = keychainRead(KEY_TOKEN_CODE)
//        set(v) { if (v != null) keychainWrite(KEY_TOKEN_CODE, v) else keychainDelete(KEY_TOKEN_CODE) }
//
//    // ── Credentials (Keychain) ────────────────────────────────────────────────
//
//    override var savedUsername: String?
//        get() = keychainRead(KEY_USERNAME)
//        set(v) { if (v != null) keychainWrite(KEY_USERNAME, v) else keychainDelete(KEY_USERNAME) }
//
//    override var savedPassword: String?
//        get() = keychainRead(KEY_PASSWORD)
//        set(v) { if (v != null) keychainWrite(KEY_PASSWORD, v) else keychainDelete(KEY_PASSWORD) }
//
//    // ── User profile (NSUserDefaults) ─────────────────────────────────────────
//
//    override var userEmail: String?
//        get() = defaults.stringForKey(KEY_USER_EMAIL)
//        set(v) { if (v != null) defaults.setObject(v, KEY_USER_EMAIL) else defaults.removeObjectForKey(KEY_USER_EMAIL) }
//
//    override var userName: String?
//        get() = defaults.stringForKey(KEY_USER_NAME)
//        set(v) { if (v != null) defaults.setObject(v, KEY_USER_NAME) else defaults.removeObjectForKey(KEY_USER_NAME) }
//
//    override var userUid: String?
//        get() = defaults.stringForKey(KEY_USER_UID)
//        set(v) { if (v != null) defaults.setObject(v, KEY_USER_UID) else defaults.removeObjectForKey(KEY_USER_UID) }
//
//    override var accessLevel: String?
//        get() = defaults.stringForKey(KEY_ACCESS_LEVEL)
//        set(v) { if (v != null) defaults.setObject(v, KEY_ACCESS_LEVEL) else defaults.removeObjectForKey(KEY_ACCESS_LEVEL) }
//
//    override var signInAsMerchant: Boolean
//        get() = defaults.boolForKey(KEY_SIGN_IN_AS_MERCHANT)
//        set(v) { defaults.setBool(v, KEY_SIGN_IN_AS_MERCHANT) }
//
//    // ── Auth details JSON (Keychain) ──────────────────────────────────────────
//
//    override var authDetailsJson: String?
//        get() = keychainRead(KEY_AUTH_DETAILS_JSON)
//        set(v) { if (v != null) keychainWrite(KEY_AUTH_DETAILS_JSON, v) else keychainDelete(KEY_AUTH_DETAILS_JSON) }
//
//    // ── Device registration (NSUserDefaults) ──────────────────────────────────
//
//    override var deviceNumber: String?
//        get() = defaults.stringForKey(KEY_DEVICE_NUMBER)
//        set(v) { if (v != null) defaults.setObject(v, KEY_DEVICE_NUMBER) else defaults.removeObjectForKey(KEY_DEVICE_NUMBER) }
//
//    override var deviceUniqueCode: String?
//        get() = defaults.stringForKey(KEY_DEVICE_UNIQUE_CODE)
//        set(v) { if (v != null) defaults.setObject(v, KEY_DEVICE_UNIQUE_CODE) else defaults.removeObjectForKey(KEY_DEVICE_UNIQUE_CODE) }
//
//    override var dasmid: String?
//        get() = defaults.stringForKey(KEY_DASMID)
//        set(v) { if (v != null) defaults.setObject(v, KEY_DASMID) else defaults.removeObjectForKey(KEY_DASMID) }
//
//    override var merchantLegalName: String?
//        get() = defaults.stringForKey(KEY_MERCHANT_LEGAL_NAME)
//        set(v) { if (v != null) defaults.setObject(v, KEY_MERCHANT_LEGAL_NAME) else defaults.removeObjectForKey(KEY_MERCHANT_LEGAL_NAME) }
//
//    // ── Device / external config JSON (NSUserDefaults) ────────────────────────
//
//    override var deviceConfigJson: String?
//        get() = defaults.stringForKey(KEY_DEVICE_CONFIG_JSON)
//        set(v) { if (v != null) defaults.setObject(v, KEY_DEVICE_CONFIG_JSON) else defaults.removeObjectForKey(KEY_DEVICE_CONFIG_JSON) }
//
//    // ── App config / URLs (NSUserDefaults) ────────────────────────────────────
//
//    override var baseUrl: String?
//        get() = defaults.stringForKey(KEY_BASE_URL)
//        set(v) { if (v != null) defaults.setObject(v, KEY_BASE_URL) else defaults.removeObjectForKey(KEY_BASE_URL) }
//
//    override var configBaseUrl: String?
//        get() = defaults.stringForKey(KEY_CONFIG_BASE_URL)
//        set(v) { if (v != null) defaults.setObject(v, KEY_CONFIG_BASE_URL) else defaults.removeObjectForKey(KEY_CONFIG_BASE_URL) }
//
//    override var transactionDetailsUrl: String?
//        get() = defaults.stringForKey(KEY_TRANSACTION_DETAILS_URL)
//        set(v) { if (v != null) defaults.setObject(v, KEY_TRANSACTION_DETAILS_URL) else defaults.removeObjectForKey(KEY_TRANSACTION_DETAILS_URL) }
//
//    // ── FCM (NSUserDefaults) ──────────────────────────────────────────────────
//
//    override var fcmToken: String?
//        get() = defaults.stringForKey(KEY_FCM_TOKEN)
//        set(v) { if (v != null) defaults.setObject(v, KEY_FCM_TOKEN) else defaults.removeObjectForKey(KEY_FCM_TOKEN) }
//
//    // ── Cart (NSUserDefaults) ─────────────────────────────────────────────────
//
//    override var cartJson: String?
//        get() = defaults.stringForKey(KEY_CART_JSON)
//        set(v) { if (v != null) defaults.setObject(v, KEY_CART_JSON) else defaults.removeObjectForKey(KEY_CART_JSON) }
//
//    // ── Payment methods (NSUserDefaults) ──────────────────────────────────────
//
//    override var paymentMethodsJson: String?
//        get() = defaults.stringForKey(KEY_PAYMENT_METHODS_JSON)
//        set(v) { if (v != null) defaults.setObject(v, KEY_PAYMENT_METHODS_JSON) else defaults.removeObjectForKey(KEY_PAYMENT_METHODS_JSON) }
//
//    override var externalConfigJson: String?
//        get() = defaults.stringForKey(KEY_EXTERNAL_CONFIG_JSON)
//        set(v) { if (v != null) defaults.setObject(v, KEY_EXTERNAL_CONFIG_JSON) else defaults.removeObjectForKey(KEY_EXTERNAL_CONFIG_JSON) }
//
//    // ── Geo restriction (NSUserDefaults) ──────────────────────────────────────
//
//    override var merchantCountriesJson: String?
//        get() = defaults.stringForKey(KEY_MERCHANT_COUNTRIES_JSON)
//        set(v) { if (v != null) defaults.setObject(v, KEY_MERCHANT_COUNTRIES_JSON) else defaults.removeObjectForKey(KEY_MERCHANT_COUNTRIES_JSON) }
//
//    // ── Biometric (NSUserDefaults) ────────────────────────────────────────────
//
//    override var isBiometricEnabled: Boolean
//        get() = defaults.boolForKey(KEY_BIOMETRIC_ENABLED)
//        set(v) { defaults.setBool(v, KEY_BIOMETRIC_ENABLED) }
//
//    // ── Generic Helpers ───────────────────────────────────────────────────────
//
//    override fun getString(key: String): String? = defaults.stringForKey(key)
//    override fun putString(key: String, value: String) { defaults.setObject(value, key) }
//    override fun getBoolean(key: String, default: Boolean): Boolean =
//        if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else default
//    override fun putBoolean(key: String, value: Boolean) { defaults.setBool(value, key) }
//
//    // ── Helpers ───────────────────────────────────────────────────────────────
//
//    override fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()
//
//    override fun clearAuthData() {
//        accessToken     = null
//        idToken         = null
//        refreshToken    = null
//        tokenExpiry     = 0L
//        userEmail       = null
//        userName        = null
//        userUid         = null
//        accessLevel     = null
//        authDetailsJson = null
//    }
//
//    override fun clearAll() {
//        defaults.dictionaryRepresentation().keys.forEach {
//            defaults.removeObjectForKey(it as String)
//        }
//        listOf(
//            KEY_ACCESS_TOKEN, KEY_ID_TOKEN, KEY_REFRESH_TOKEN,
//            KEY_TOKEN_CODE, KEY_USERNAME, KEY_PASSWORD, KEY_AUTH_DETAILS_JSON
//        ).forEach { keychainDelete(it) }
//    }
//
//    // ── Keys ──────────────────────────────────────────────────────────────────
//
//    private const val KEY_ACCESS_TOKEN            = "accessToken"
//    private const val KEY_ID_TOKEN                = "idToken"
//    private const val KEY_REFRESH_TOKEN           = "refreshToken"
//    private const val KEY_TOKEN_EXPIRY            = "tokenExpiry"
//    private const val KEY_TOKEN_VERIFIED          = "tokenVerified"
//    private const val KEY_TOKEN_CODE              = "tokenCode"
//    private const val KEY_USERNAME                = "savedUsername"
//    private const val KEY_PASSWORD                = "savedPassword"
//    private const val KEY_USER_EMAIL              = "userEmail"
//    private const val KEY_USER_NAME               = "userName"
//    private const val KEY_USER_UID                = "userUid"
//    private const val KEY_ACCESS_LEVEL            = "accessLevel"
//    private const val KEY_SIGN_IN_AS_MERCHANT     = "signInAsMerchant"
//    private const val KEY_AUTH_DETAILS_JSON       = "authDetailsJson"
//    private const val KEY_DEVICE_NUMBER           = "deviceNumber"
//    private const val KEY_DEVICE_UNIQUE_CODE      = "deviceUniqueCode"
//    private const val KEY_DASMID                  = "dasmid"
//    private const val KEY_MERCHANT_LEGAL_NAME     = "merchantLegalName"
//    private const val KEY_DEVICE_CONFIG_JSON      = "deviceConfigJson"
//    private const val KEY_BASE_URL                = "baseUrl"
//    private const val KEY_CONFIG_BASE_URL         = "configBaseUrl"
//    private const val KEY_TRANSACTION_DETAILS_URL = "transactionDetailsUrl"
//    private const val KEY_FCM_TOKEN               = "fcmToken"
//    private const val KEY_CART_JSON               = "cartJson"
//    private const val KEY_PAYMENT_METHODS_JSON    = "paymentMethodsJson"
//    private const val KEY_EXTERNAL_CONFIG_JSON    = "externalConfigJson"
//    private const val KEY_MERCHANT_COUNTRIES_JSON = "merchantCountriesJson"
//    private const val KEY_BIOMETRIC_ENABLED       = "biometricEnabled"
//}

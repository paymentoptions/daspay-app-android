package com.paymentoptions.pos.storage

import com.russhwolf.settings.Settings

/**
 * Interface defining all persistent storage keys and helpers.
 */
interface StorageInterface {

    fun init(settings: Settings)

    // ── Tokens ────────────────────────────────────────────────────────────────
    var accessToken: String?
    var idToken: String?
    var refreshToken: String?
    var tokenExpiry: Long

    // ── Token verification ────────────────────────────────────────────────────
    var tokenVerified: Boolean
    var tokenCode: String?

    // ── Credentials ───────────────────────────────────────────────────────────
    var savedUsername: String?
    var savedPassword: String?

    // ── User profile ──────────────────────────────────────────────────────────
    var userEmail: String?
    var userName: String?
    var userUid: String?
    var accessLevel: String?
    var signInAsMerchant: Boolean

    // ── Auth details JSON ─────────────────────────────────────────────────────
    var authDetailsJson: String?

    // ── Device registration ───────────────────────────────────────────────────
    var deviceNumber: String?
    var deviceUniqueCode: String?
    var dasmid: String?
    var merchantLegalName: String?

    // ── Device / external config JSON ─────────────────────────────────────────
    var deviceConfigJson: String?

    // ── App config / URLs ─────────────────────────────────────────────────────
    var baseUrl: String?
    var configBaseUrl: String?
    var transactionDetailsUrl: String?

    // ── FCM ───────────────────────────────────────────────────────────────────
    var fcmToken: String?

    // ── Cart ──────────────────────────────────────────────────────────────────
    var cartJson: String?

    // ── Payment methods ───────────────────────────────────────────────────────
    var paymentMethodsJson: String?
    var externalConfigJson: String?

    // ── Geo restriction ───────────────────────────────────────────────────────
    var merchantCountriesJson: String?

    // ── Biometric ─────────────────────────────────────────────────────────────
    var isBiometricEnabled: Boolean

    // ── Generic helpers ───────────────────────────────────────────────────────
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)

    // ── Helpers ───────────────────────────────────────────────────────────────
    fun isLoggedIn(): Boolean
    fun clearAuthData()
    fun clearAll()
}

/**
 * Cross-platform persistent storage singleton.
 * Uses 'expect val' to avoid IR backend bugs related to 'expect object'.
 */
expect val AppStorage: StorageInterface

package com.paymentoptions.pos.utils

import kotlin.concurrent.Volatile

/**
 * Singleton to hold the Keychain provider instance injected from the iOS App.
 */
object KeychainHelper {
    @Volatile
    var provider: KeychainProvider? = null

    fun get(key: String): String? {
        return provider?.get(key)
    }

    fun put(key: String, value: String) {
        provider?.put(key, value)
    }
}

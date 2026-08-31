package com.paymentoptions.pos.utils

/**
 * Interface to bridge Keychain operations to the native iOS implementation.
 */
interface KeychainProvider {
    fun get(key: String): String?
    fun put(key: String, value: String)
}

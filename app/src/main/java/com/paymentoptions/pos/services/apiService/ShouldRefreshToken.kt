package com.paymentoptions.pos.services.apiService

fun shouldRefreshToken(expiry: Long?): Boolean {
    val expiryTimestampInMilliseconds = 1000 * (expiry ?: 0)
    val currentTimestampInMilliseconds = System.currentTimeMillis()

    return currentTimestampInMilliseconds > expiryTimestampInMilliseconds
}
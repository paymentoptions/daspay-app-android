package com.paymentoptions.pos.services.apiService

fun shouldRefreshToken(authDetails: SignInResponse?): Boolean {
    val expiryTimestampInMilliseconds = 1000 * (authDetails?.data?.exp ?: 0)
    val currentTimestampInMilliseconds = System.currentTimeMillis()

    return currentTimestampInMilliseconds > expiryTimestampInMilliseconds
}
package com.paymentoptions.pos.apiService

fun shouldRefreshToken(authDetails: SignInResponse?): Boolean {
//    val hasExpired = authDetails === null || authDetails.data.exp > System.currentTimeMillis()
//    return hasExpired

    return true
}
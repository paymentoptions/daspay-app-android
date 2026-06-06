package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.getPushToken
import com.paymentoptions.pos.network.SignInResponse
import com.paymentoptions.pos.storage.AppStorage

/**
 * Signs in using stored credentials.
 * On success: saves auth details and fetches the push token.
 * Firebase FCM is resolved via [getPushToken] (expect/actual — Android provides real token, iOS returns null).
 */
suspend fun autoSignIn(deviceNumber: String): SignInResponse? {
    val username = AppStorage.savedUsername ?: return null
    val password = AppStorage.savedPassword ?: return null

    val response = signIn(username, password, deviceNumber) ?: return null

    if (response.success == true && response.data != null) {
        val data = response.data
        // Persist auth data
        AppStorage.accessToken    = data.token.accessToken
        AppStorage.idToken        = data.token.idToken
        AppStorage.refreshToken   = data.token.refreshToken
        AppStorage.tokenExpiry    = data.exp * 1000L
        AppStorage.userEmail      = data.email
        AppStorage.userName       = data.name
        AppStorage.userUid        = data.uid
        AppStorage.accessLevel    = data.accessLevel?.name
        AppStorage.signInAsMerchant = data.signInAsMerchant

        // Fetch push token (Android: Firebase, iOS: null)
        val pushToken = getPushToken()
        if (pushToken != null) AppStorage.fcmToken = pushToken

        // Save merchant countries for geo-restriction
        AppStorage.merchantCountriesJson = data.subsidiaries.joinToString(",")
    }

    return response
}

package com.paymentoptions.pos.device

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.paymentoptions.pos.services.apiService.SignInResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


const val sharedPreferencesLabel: String = "my_prefs"

class SharedPreferences {
    companion object {
        fun saveBoolean(context: Context, key: String, value: Boolean) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean(key, value)
                apply()
            }
        }

        fun getBoolean(context: Context, key: String): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val biometricsEnabled = sharedPreferences.getBoolean(key, false)
            return biometricsEnabled
        }

        fun saveKeyValue(context: Context, key: String, value: String) {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(key, value)
                apply()
            }
        }

        fun saveBiometricsStatus(context: Context, status: Boolean = false) {
            saveBoolean(context, "biometrics", status)
        }

        fun getBiometricsStatus(context: Context): Boolean {
            return getBoolean(context, "biometrics")
        }

        fun getImmersiveModeStatus(context: Context): Boolean {
            return getBoolean(context, "immersive")
        }

        fun saveImmersiveModeStatus(context: Context, status: Boolean = false) {
            saveBoolean(context, "immersive", status)
        }

        fun saveAuthDetails(context: Context, authDetails: SignInResponse) = runBlocking {
            val authDetailsString = Json.encodeToString(authDetails)
            saveKeyValue(context, "auth_details", authDetailsString)
        }

        fun getAuthDetails(context: Context): SignInResponse? {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val authDetailsString = sharedPreferences.getString("auth_details", null)
            val authDetailsJson =
                authDetailsString?.let { Json.decodeFromString<SignInResponse>(it) }

//            val decodedToken = decodeJwtPayload(authDetailsJson!!.data.token.idToken)
//            println("decodedToken: $decodedToken")

            return authDetailsJson
        }

        fun clearSharedPreferences(context: Context) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)

            with(sharedPreferences.edit()) {
                clear().apply()
            }
        }

        fun saveFcmToken(context: Context, token: String) {
            val sharedPref = context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("fcm_token", token)
                apply()
            }
        }

        fun getFcmToken(context: Context): String? {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val fcmToken = sharedPreferences.getString("fcm_token", null)

            return fcmToken
        }
    }
}
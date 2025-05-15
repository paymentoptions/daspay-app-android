package com.paymentoptions.pos.device

import android.content.Context
import com.paymentoptions.pos.services.apiService.SignInResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


const val sharedPreferencesLabel: String = "my_prefs"

class SharedPreferences {
    companion object {
        fun saveBoolean(context: Context, key: String, value: Boolean) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean(key, value)
                apply()
            }
        }

        fun getBoolean(context: Context, key: String) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)
            val biometricsEnabled = sharedPreferences.getBoolean(key, false)
            return@runBlocking biometricsEnabled

        }

        fun saveKeyValue(context: Context, key: String, value: String) {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(key, value)
                apply()
            }
        }

        fun saveBiometricsStatus(context: Context, status: Boolean) = runBlocking {
            saveBoolean(context, "biometrics", status)
        }

        fun getBiometricsStatus(context: Context) = runBlocking {
            return@runBlocking getBoolean(context, "biometrics")
        }

        fun saveAuthDetails(context: Context, authDetails: SignInResponse) = runBlocking {
            val authDetailsString = Json.encodeToString(authDetails)
            saveKeyValue(context, "auth_details", authDetailsString)
        }

        fun getAuthDetails(context: Context): SignInResponse? = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)
            val authDetailsString = sharedPreferences.getString("auth_details", null)
            val authDetailsJson =
                authDetailsString?.let { Json.decodeFromString<SignInResponse>(it) }

            return@runBlocking authDetailsJson
        }

        fun clearSharedPreferences(context: Context) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)

            with(sharedPreferences.edit()) {
                clear().apply()
            }
        }

        fun getFcmToken(context: Context): String? {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)
            val fcmToken = sharedPreferences.getString("fcm_token", null)

            return fcmToken
        }
    }
}
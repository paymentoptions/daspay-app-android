package com.paymentoptions.pos.device

import android.content.Context
import com.paymentoptions.pos.apiService.SignInResponse
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

        fun saveInt(context: Context, key: String, value: Int) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putInt(key, value)
                apply()
            }
        }

        fun getInt(context: Context, key: String) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)
            val biometricsEnabled = sharedPreferences.getInt(key, 0)
            return@runBlocking biometricsEnabled

        }

        fun saveInt(context: Context, key: String, value: String) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(key, value)
                apply()
            }
        }

        fun getString(context: Context, key: String) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, Context.MODE_PRIVATE)
            val biometricsEnabled = sharedPreferences.getBoolean("biometrics", false)
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
            SharedPreferences.saveBoolean(context, "biometrics", status)
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
    }
}
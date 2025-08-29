package com.paymentoptions.pos.device

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.paymentoptions.pos.services.apiService.ExternalConfigurationResponse
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.ui.composables.screens._flow.foodorder.Cart
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import android.util.Log

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

        fun getCart(context: Context): Cart? {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)

            val cartJsonString = sharedPreferences.getString("cart", null)

            val cart =
                cartJsonString?.let { Json.decodeFromString<Cart>(it) }

            return cart
        }

        fun saveCart(context: Context, cart: Cart) {
            val cartJsonString = cart.toJson()
            saveKeyValue(context, "cart", cartJsonString)
        }

        fun clearSavedCart(context: Context) {
            saveKeyValue(context, "cart", "")
        }

        fun saveDeviceConfiguration(context: Context, config: ExternalConfigurationResponse) {
            val configJsonString = Json.encodeToString(config)
            saveKeyValue(context, "device_config", configJsonString)
        }

        fun getDeviceConfiguration(context: Context): ExternalConfigurationResponse? {
            val sharedPreferences = context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val configJsonString = sharedPreferences.getString("device_config", null)
            return configJsonString?.let { Json.decodeFromString<ExternalConfigurationResponse>(it)}
        }
    }
}
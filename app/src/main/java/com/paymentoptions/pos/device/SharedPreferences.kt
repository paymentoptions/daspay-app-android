package com.paymentoptions.pos.device

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.paymentoptions.pos.services.apiService.DevicePaymentMethod_Apms
import com.paymentoptions.pos.services.apiService.DevicePaymentMethod_Schemes
import com.paymentoptions.pos.services.apiService.ExternalConfigurationResponse
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.Cart
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

            val cart = cartJsonString?.let { Json.decodeFromString<Cart>(it) }

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
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val configJsonString = sharedPreferences.getString("device_config", null)
            return configJsonString?.let { Json.decodeFromString<ExternalConfigurationResponse>(it) }
        }

        fun saveTokenStatus(context: Context, isVerified: Boolean) {
            val sharedPref = context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("token_verified", isVerified)
                apply()
            }
        }

        fun getTokenStatus(context: Context): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val isVerified = sharedPreferences.getBoolean("token_verified", false)

            return isVerified
        }
    }
}

fun getTransactionCurrency(context: Context): String {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var transactionCurrency = ""

    externalDeviceConfiguration?.let {
        transactionCurrency =
            it.data.paymentMethod.firstOrNull()?.TransactionCCY?.firstOrNull() ?: ""

    }

    return transactionCurrency
}

fun getSettlementCurrency(context: Context): String {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var settlementCurrency = ""

    externalDeviceConfiguration?.let {
        settlementCurrency = it.data.paymentMethod.firstOrNull()?.SettlementCCY ?: ""
    }
    return settlementCurrency
}

fun getDasmid(context: Context): String {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var dasmid = ""

    externalDeviceConfiguration?.let {
        dasmid = it.data.paymentMethod.firstOrNull()?.DASMID ?: ""
    }
    return dasmid
}

fun getSchemes(context: Context): DevicePaymentMethod_Schemes {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var schemes = DevicePaymentMethod_Schemes()

    externalDeviceConfiguration?.let {
        schemes = it.data.paymentMethod.firstOrNull()?.schemes ?: DevicePaymentMethod_Schemes()
    }
    return schemes
}

fun getApms(context: Context): DevicePaymentMethod_Apms {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var apms = DevicePaymentMethod_Apms()

    externalDeviceConfiguration?.let {
        apms = it.data.paymentMethod.firstOrNull()?.apms ?: DevicePaymentMethod_Apms()
    }
    return apms
}
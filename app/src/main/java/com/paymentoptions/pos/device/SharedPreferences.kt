package com.paymentoptions.pos.device

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.AccessLevel
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
        private var accessLevel : AccessLevel? = null
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
            accessLevel = authDetails.data.accessLevel
        }

        fun getAuthDetails(context: Context): SignInResponse? {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val authDetailsString = sharedPreferences.getString("auth_details", null)
            val authDetailsJson =
                authDetailsString?.let { Json.decodeFromString<SignInResponse>(it) }

            return authDetailsJson
        }

        fun isAdmin(context: Context) : Boolean {
            if(accessLevel == null) {
                val authDetails = getAuthDetails(context)
                return authDetails?.data?.accessLevel == AccessLevel.ADMIN
            } else {
                return accessLevel == AccessLevel.ADMIN
            }
        }

        fun isStaff(context: Context) : Boolean {
            if(accessLevel == null) {
                val authDetails = getAuthDetails(context)
                return authDetails?.data?.accessLevel == AccessLevel.STAFF
            } else {
                return accessLevel == AccessLevel.STAFF
            }
        }


        /**fun clearSharedPreferences(context: Context) = runBlocking {
        val sharedPreferences =
        context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)

        with(sharedPreferences.edit()) {
        clear().apply()
        }
        }**/
        fun clearSharedPreferences(context: Context) = runBlocking {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)

            with(sharedPreferences.edit()) {
                remove("auth_details")
                apply()
            }
            accessLevel = null

        }

        fun saveOtp(context: Context, otp: String){
            val sharedPref = context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("saved_otp", otp)
                apply()
            }
        }

        fun getOtp(context: Context): String?{
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val otp = sharedPreferences.getString("saved_otp", null)
            return otp
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

            val cartJsonString = sharedPreferences.getString("cart", "{}")

            val cart = cartJsonString?.let { Json.decodeFromString<Cart>(it) }

            return cart
        }

        fun saveCart(context: Context, cart: Cart) {
            val cartJsonString = cart.toJson()
            saveKeyValue(context, "cart", cartJsonString)
        }

        fun clearSavedCart(context: Context) {
            context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE).apply {
                edit().remove("cart")
            }

            AppLogger.debug("cart cleared ->")

            val cart = getCart(context)
            AppLogger.debug("cart: $cart")
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

        fun saveTokenStatus(context: Context, tokenCode: String, isVerified: Boolean) {
            val sharedPref = context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)

            with(sharedPref.edit()) {
                putBoolean("token_verified", isVerified)
                putString("token_code", tokenCode)
                apply()
            }
        }

        fun getTokenStatus(context: Context): Pair<Boolean, String> {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val isVerified = sharedPreferences.getBoolean("token_verified", false)
            val tokenCode = sharedPreferences.getString("token_code", null)

            return Pair<Boolean, String>(isVerified, tokenCode ?: "")
        }

        fun saveCredentials(context: Context, email: String, password: String) {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("saved_email", email)
                putString("saved_password", password) // Saving plain text password as requested
                apply()
            }
        }

        fun getSavedCredentials(context: Context): Triple<String?, String?, String?> {
            val sharedPreferences =
                context.getSharedPreferences(sharedPreferencesLabel, MODE_PRIVATE)
            val email = sharedPreferences.getString("saved_email", null)
            val password = sharedPreferences.getString("saved_password", null)
            val otp = sharedPreferences.getString("saved_otp", null)
            return Triple(email, password, otp)
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

//SOFTPOS DASMID
fun getTapPayDasmid(context: Context): String {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var dasmid = ""

    externalDeviceConfiguration?.let {
        for (paymentMethod in it.data.paymentMethod) {
            if (paymentMethod.Type == "SOFTPOS") {
                dasmid = paymentMethod.DASMID
                break
            }
        }
    }
    return dasmid
}

//QP DASMID
fun getQRDasmid(context: Context): String {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var dasmid = ""

    externalDeviceConfiguration?.let {
        for (paymentMethod in it.data.paymentMethod) {
            if (paymentMethod.Type == "QR") {
                dasmid = paymentMethod.DASMID
                break
            }
        }
    }
    return dasmid
}

//PBl DASMID
fun getPayByLinkDasmid(context: Context): String {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var dasmid = ""

    externalDeviceConfiguration?.let {
        for (paymentMethod in it.data.paymentMethod) {
            if (paymentMethod.Type == "PBL") {
                dasmid = paymentMethod.DASMID
                break
            }
        }
    }
    return dasmid
}

//this function will extract schemes from the external device configuration in which the payment method type is SOFTPOS
fun getSchemes(context: Context): DevicePaymentMethod_Schemes {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var schemes = DevicePaymentMethod_Schemes()

    externalDeviceConfiguration?.let {
        for (paymentMethod in it.data.paymentMethod) {
            if (paymentMethod.Type == "SOFTPOS") {
                schemes = paymentMethod.schemes
                break
            }
        }
    }
    return schemes
}

//this function will extract apms from the external device configuration in which the payment method type is QR
fun getApms(context: Context): DevicePaymentMethod_Apms {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var apms = DevicePaymentMethod_Apms()

    externalDeviceConfiguration?.let {
        for (paymentMethod in it.data.paymentMethod) {
            if (paymentMethod.Type == "QR") {
                apms = paymentMethod.apms
                break
            }
        }
        //apms = it.data.paymentMethod.firstOrNull()?.apms ?: DevicePaymentMethod_Apms()
    }
    return apms
}

fun getDeviceId(context: Context): String? {
    val externalDeviceConfiguration = SharedPreferences.getDeviceConfiguration(context)
    var deviceId: String? = null

    externalDeviceConfiguration?.let {
        deviceId = it.data.deviceInfo.DeviceID
    }
    return deviceId
}

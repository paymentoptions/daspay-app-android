package com.paymentoptions.pos.device

import com.paymentoptions.pos.network.AppConfig
import com.paymentoptions.pos.network.DevicePaymentMethod_Apms
import com.paymentoptions.pos.network.DevicePaymentMethod_Schemes
import com.paymentoptions.pos.network.ExternalConfigurationResponse
import com.paymentoptions.pos.network.SignInResponse

/**
 * Compatibility shim: all persistence delegates to AppStorage.
 */
object DPStorageManager {

    fun saveBoolean(key: String, value: Boolean) = dpSaveBoolean(key, value)

    fun getBoolean(key: String): Boolean = dpGetBoolean(key)

    fun saveKeyValue(key: String, value: String) = dpSaveKeyValue(key, value)

    fun getKeyValue(key: String): String? = dpGetKeyValue(key)

    fun saveBiometricsStatus(status: Boolean = false) = dpSaveBiometricsStatus(status)

    fun getBiometricsStatus(): Boolean = dpGetBiometricsStatus()

    fun isAdmin(): Boolean = dpIsAdmin()

    fun isStaff(): Boolean = dpIsStaff()

    fun saveAuthDetails(authDetails: SignInResponse) = dpSaveAuthDetails(authDetails)

    fun getAuthDetails(): SignInResponse? = dpGetAuthDetails()

    fun clearSharedPreferences() = dpClearSharedPreferences()

    fun saveCredentials(email: String, password: String) = dpSaveCredentials(email, password)

    fun getSavedCredentials(): Triple<String?, String?, String?> = dpGetSavedCredentials()

    fun saveTokenStatus(tokenCode: String, isVerified: Boolean) = dpSaveTokenStatus(tokenCode, isVerified)

    fun getTokenStatus(): Pair<Boolean, String> = dpGetTokenStatus()

    fun saveFcmToken(token: String) = dpSaveFcmToken(token)

    fun getFcmToken(): String? = dpGetFcmToken()

    fun saveDeviceConfiguration(config: ExternalConfigurationResponse) = dpSaveDeviceConfiguration(config)

    fun getDeviceConfiguration(): ExternalConfigurationResponse? = dpGetDeviceConfiguration()

    fun getTransactionCurrency(): String = dpGetTransactionCurrency()

    fun getSettlementCurrency(): String = dpGetSettlementCurrency()

    fun getTapPayDasmid(): String = dpGetTapPayDasmid()

    fun getQRDasmid(): String = dpGetQRDasmid()

    fun getPayByLinkDasmid(): String = dpGetPayByLinkDasmid()

    fun getSchemes(): DevicePaymentMethod_Schemes = dpGetSchemes()

    fun getApms(): DevicePaymentMethod_Apms = dpGetApms()

    fun storeAppConfig(appConfig: AppConfig) = dpStoreAppConfig(appConfig)

    fun getTokenExpiry(): Long = dpGetTokenExpiry()

    fun getBaseUrl(): String? = dpGetBaseUrl()

    fun getTransactionDetailsUrl(): String? = dpGetTransactionDetailsUrl()
}

internal expect fun dpSaveBoolean(key: String, value: Boolean)
internal expect fun dpGetBoolean(key: String): Boolean
internal expect fun dpSaveKeyValue(key: String, value: String)
internal expect fun dpGetKeyValue(key: String): String?
internal expect fun dpSaveBiometricsStatus(status: Boolean)
internal expect fun dpGetBiometricsStatus(): Boolean
internal expect fun dpIsAdmin(): Boolean
internal expect fun dpIsStaff(): Boolean
internal expect fun dpSaveAuthDetails(authDetails: SignInResponse)
internal expect fun dpGetAuthDetails(): SignInResponse?
internal expect fun dpClearSharedPreferences()
internal expect fun dpSaveCredentials(email: String, password: String)
internal expect fun dpGetSavedCredentials(): Triple<String?, String?, String?>
internal expect fun dpSaveTokenStatus(tokenCode: String, isVerified: Boolean)
internal expect fun dpGetTokenStatus(): Pair<Boolean, String>
internal expect fun dpSaveFcmToken(token: String)
internal expect fun dpGetFcmToken(): String?
internal expect fun dpSaveDeviceConfiguration(config: ExternalConfigurationResponse)
internal expect fun dpGetDeviceConfiguration(): ExternalConfigurationResponse?
internal expect fun dpGetTransactionCurrency(): String
internal expect fun dpGetSettlementCurrency(): String
internal expect fun dpGetTapPayDasmid(): String
internal expect fun dpGetQRDasmid(): String
internal expect fun dpGetPayByLinkDasmid(): String
internal expect fun dpGetSchemes(): DevicePaymentMethod_Schemes
internal expect fun dpGetApms(): DevicePaymentMethod_Apms
internal expect fun dpStoreAppConfig(appConfig: AppConfig)
internal expect fun dpGetTokenExpiry(): Long
internal expect fun dpGetBaseUrl(): String?
internal expect fun dpGetTransactionDetailsUrl(): String?

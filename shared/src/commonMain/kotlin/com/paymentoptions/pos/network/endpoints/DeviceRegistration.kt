package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.getDeviceManufacturer
import com.paymentoptions.pos.getDeviceOs
import com.paymentoptions.pos.getDeviceOsVersion
import com.paymentoptions.pos.network.ApiEndpoints
import com.paymentoptions.pos.network.CompleteDeviceRegistrationRequest
import com.paymentoptions.pos.network.CompleteDeviceRegistrationResponse
import com.paymentoptions.pos.network.ConfigurationManager
import com.paymentoptions.pos.network.DeviceMetadata
import com.paymentoptions.pos.network.ExternalConfigurationResponse
import com.paymentoptions.pos.network.KtorClient
import com.paymentoptions.pos.network.applyDaspayHeaders
import com.paymentoptions.pos.network.throwIfNotSuccess
import com.paymentoptions.pos.storage.AppStorage
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Completes the device registration using an OTP.
 */
suspend fun completeDeviceRegistration(
    otp: String, deviceNumber: String
): Result<CompleteDeviceRegistrationResponse> {
    return try {
        // Device identifiers and metadata are resolved via Platform wrappers
        val deviceMetadata = DeviceMetadata(
            os = getDeviceOs(),
            version = getDeviceOsVersion(),
            manufacturer = getDeviceManufacturer()
        )

        val requestBody = CompleteDeviceRegistrationRequest(
            UniqueCode = otp,
            DeviceNumber = deviceNumber,
            DeviceMetadata = deviceMetadata
        )
        AppStorage.deviceNumber = deviceNumber

        val response = KtorClient.instance.post(ConfigurationManager.url(ApiEndpoints.COMPLETE_REGISTRATION)) {
            contentType(ContentType.Application.Json)
            applyDaspayHeaders()
            setBody(requestBody)
        }
        response.throwIfNotSuccess("completeDeviceRegistration")

        Result.success(response.body<CompleteDeviceRegistrationResponse>())
    } catch (e: Exception) {
        // Ktor exceptions are handled here
        Result.failure(e)
    }
}

/**
 * Fetches the external device configuration.
 */
suspend fun getExternalDeviceConfiguration(
    otp: String, deviceNumber : String
): Result<ExternalConfigurationResponse> {
    return try {
        val uniqueCode = otp

        val response = KtorClient.instance.get(ConfigurationManager.url(ApiEndpoints.DEVICE_CONFIGURATION)) {
            applyDaspayHeaders()
            parameter("DeviceNumber", deviceNumber)
            parameter("UniqueCode", uniqueCode)
        }
        response.throwIfNotSuccess("getExternalDeviceConfiguration")

        Result.success(response.body<ExternalConfigurationResponse>())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

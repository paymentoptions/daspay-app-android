package com.paymentoptions.pos.device

actual object GeoRestrictionManager {

    actual fun saveMerchantCountry(subsidiaries: List<String>) {
        val country = subsidiaries.firstOrNull()?.uppercase() ?: return
        DPStorageManager.saveKeyValue("merchant_country", country)
        DPStorageManager.saveKeyValue("device_registered_country", country)
    }

    actual fun getMerchantCountry(): String {
        return DPStorageManager.getKeyValue("merchant_country") ?: ""
    }

    actual fun checkRestriction(): GeoRestrictionResult {
        val merchantCountry = getMerchantCountry()
        return GeoRestrictionResult(
            isRestricted = false,
            merchantCountry = merchantCountry,
            deviceRegisteredCountry = merchantCountry,
            currentLocationCountry = null,
            message = "",
        )
    }
}

package com.paymentoptions.pos.device

data class GeoRestrictionResult(
    val isRestricted: Boolean,
    val merchantCountry: String,
    val deviceRegisteredCountry: String,
    val currentLocationCountry: String?,
    val message: String,
)

expect object GeoRestrictionManager {
    fun saveMerchantCountry(subsidiaries: List<String>)
    fun getMerchantCountry(): String
    fun checkRestriction(): GeoRestrictionResult
}

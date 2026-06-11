package com.paymentoptions.pos.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.paymentoptions.pos.logger.AppLogger
import java.util.Locale

data class GeoRestrictionResult(
    val isRestricted: Boolean,
    val merchantCountry: String,
    val deviceRegisteredCountry: String,
    val currentLocationCountry: String?,
    val message: String,
)

object GeoRestrictionManager {

    private const val PREF_MERCHANT_COUNTRY = "merchant_country"
    private const val PREF_DEVICE_REGISTERED_COUNTRY = "device_registered_country"

    /**
     * Save the merchant's registered country (from subsidiaries) after login.
     */
    fun saveMerchantCountry(context: Context, subsidiaries: List<String>) {
        val country = subsidiaries.firstOrNull()?.uppercase() ?: return
        DPStorageManager.saveKeyValue(PREF_MERCHANT_COUNTRY, country)
        DPStorageManager.saveKeyValue(PREF_DEVICE_REGISTERED_COUNTRY, country)
        AppLogger.debug("GeoRestriction: Saved merchant country=$country from subsidiaries=$subsidiaries")
    }

    /**
     * Get the saved merchant country code.
     */
    fun getMerchantCountry(context: Context): String {
        return DPStorageManager.getKeyValue(PREF_MERCHANT_COUNTRY) ?: ""
    }

    /**
     * Get the device's current location country code using reverse geocoding.
     * Returns null if location permissions are not granted or location is unavailable.
     */
    fun getCurrentLocationCountry(context: Context): String? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            AppLogger.debug("GeoRestriction: Location permissions not granted")
            return null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (location == null) {
            AppLogger.debug("GeoRestriction: Could not get device location")
            return null
        }

        return try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val countryCode = addresses?.firstOrNull()?.countryCode?.uppercase()
            AppLogger.debug("GeoRestriction: Current location country=$countryCode (lat=${location.latitude}, lng=${location.longitude})")
            countryCode
        } catch (e: Exception) {
            AppLogger.error("GeoRestriction: Geocoder error: ${e.message}")
            null
        }
    }

    /**
     * Check if the device is geo-restricted.
     * Compares merchant registered country with current device location.
     * If location is unavailable, allows usage (fail-open).
     */
    fun checkRestriction(context: Context): GeoRestrictionResult {
        val merchantCountry = getMerchantCountry(context)
        val deviceRegisteredCountry = DPStorageManager.getKeyValue(PREF_DEVICE_REGISTERED_COUNTRY) ?: merchantCountry
        val currentCountry = getCurrentLocationCountry(context)

        if (merchantCountry.isEmpty()) {
            AppLogger.debug("GeoRestriction: No merchant country saved, allowing access")
            return GeoRestrictionResult(
                isRestricted = false,
                merchantCountry = merchantCountry,
                deviceRegisteredCountry = deviceRegisteredCountry,
                currentLocationCountry = currentCountry,
                message = "",
            )
        }

        val isRestricted = when {
            // If we can't determine location, allow (fail-open)
            currentCountry == null -> false
            // If current location doesn't match merchant country
            !currentCountry.equals(merchantCountry, ignoreCase = true) -> true
            // If current location doesn't match device registered country
            !currentCountry.equals(deviceRegisteredCountry, ignoreCase = true) -> true
            else -> false
        }

        val countryName = getCountryName(merchantCountry)
        val message = if (isRestricted) {
            "This device is restricted to $countryName usage only. Transactions are blocked."
        } else {
            ""
        }

        AppLogger.debug("GeoRestriction: merchantCountry=$merchantCountry, deviceRegistered=$deviceRegisteredCountry, current=$currentCountry, restricted=$isRestricted")

        return GeoRestrictionResult(
            isRestricted = isRestricted,
            merchantCountry = merchantCountry,
            deviceRegisteredCountry = deviceRegisteredCountry,
            currentLocationCountry = currentCountry,
            message = message,
        )
    }

    private fun getCountryName(countryCode: String): String {
        return try {
            @Suppress("DEPRECATION")
            Locale("", countryCode).displayCountry
        } catch (_: Exception) {
            countryCode
        }
    }
}


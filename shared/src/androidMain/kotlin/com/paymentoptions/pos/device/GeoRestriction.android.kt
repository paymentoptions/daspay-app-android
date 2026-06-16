package com.paymentoptions.pos.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.paymentoptions.pos.currentActivity
import com.paymentoptions.pos.logger.AppLogger
import java.util.Locale

private const val PREF_MERCHANT_COUNTRY = "merchant_country"
private const val PREF_DEVICE_REGISTERED_COUNTRY = "device_registered_country"

actual object GeoRestrictionManager {

    actual fun saveMerchantCountry(subsidiaries: List<String>) {
        val country = subsidiaries.firstOrNull()?.uppercase() ?: return
        DPStorageManager.saveKeyValue(PREF_MERCHANT_COUNTRY, country)
        DPStorageManager.saveKeyValue(PREF_DEVICE_REGISTERED_COUNTRY, country)
        AppLogger.debug("GeoRestriction: Saved merchant country=$country from subsidiaries=$subsidiaries")
    }

    actual fun getMerchantCountry(): String {
        return DPStorageManager.getKeyValue(PREF_MERCHANT_COUNTRY) ?: ""
    }

    actual fun checkRestriction(): GeoRestrictionResult {
        val merchantCountry = getMerchantCountry()
        val deviceRegisteredCountry = DPStorageManager.getKeyValue(PREF_DEVICE_REGISTERED_COUNTRY) ?: merchantCountry
        val currentCountry = getCurrentLocationCountry()

        if (merchantCountry.isEmpty()) {
            return GeoRestrictionResult(
                isRestricted = false,
                merchantCountry = merchantCountry,
                deviceRegisteredCountry = deviceRegisteredCountry,
                currentLocationCountry = currentCountry,
                message = "",
            )
        }

        val isRestricted = when {
            currentCountry == null -> false
            !currentCountry.equals(merchantCountry, ignoreCase = true) -> true
            !currentCountry.equals(deviceRegisteredCountry, ignoreCase = true) -> true
            else -> false
        }

        val countryName = getCountryName(merchantCountry)
        val message = if (isRestricted) {
            "This device is restricted to $countryName usage only. Transactions are blocked."
        } else {
            ""
        }

        return GeoRestrictionResult(
            isRestricted = isRestricted,
            merchantCountry = merchantCountry,
            deviceRegisteredCountry = deviceRegisteredCountry,
            currentLocationCountry = currentCountry,
            message = message,
        )
    }

    private fun getCurrentLocationCountry(): String? {
        val context = currentActivity ?: return null
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: return null

        return try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.countryCode?.uppercase()
        } catch (e: Exception) {
            AppLogger.error("GeoRestriction: Geocoder error: ${e.message}")
            null
        }
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

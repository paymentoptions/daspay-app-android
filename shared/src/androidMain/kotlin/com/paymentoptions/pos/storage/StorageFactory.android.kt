package com.paymentoptions.pos.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Single entry point for MainActivity.
 * Hides the [Settings] type so the app module doesn't need multiplatform-settings on its classpath.
 */
fun initAppStorage(context: Context) = AppStorage.init(createEncryptedSettings(context))

/**
 * Creates an encrypted [Settings] backed by [EncryptedSharedPreferences].
 *
 * Call this once in [MainActivity] before [AppStorage.init].
 */
fun createEncryptedSettings(context: Context): Settings {
    val prefs = createEncryptedPrefs(context)
    return SharedPreferencesSettings(prefs)
}

private const val PREFS_NAME = "my_prefs"

private fun createEncryptedPrefs(context: Context): android.content.SharedPreferences {
    return try {
        buildEncryptedPrefs(context)
    } catch (e: Throwable) {
        // Clear everything on first failure
        runCatching {
            val ks = java.security.KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
            ks.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        }
        clearCorruptedPrefsFiles(context)
        try {
            buildEncryptedPrefs(context)
        } catch (e2: Throwable) {
            // Last resort: clear common pref files again and try once more
            clearCorruptedPrefsFiles(context)
            buildEncryptedPrefs(context)
        }
    }
}

private fun buildEncryptedPrefs(context: Context): android.content.SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    return EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

private fun clearCorruptedPrefsFiles(context: Context) {
    val dir = context.applicationInfo.dataDir + "/shared_prefs/"
    listOf(
        "$dir$PREFS_NAME.xml",
        "${dir}__androidx_security_crypto_encrypted_prefs__$PREFS_NAME.xml",
        "${dir}__androidx_security_crypto_encrypted_prefs__.xml"
    ).forEach { path ->
        val f = java.io.File(path)
        if (f.exists()) f.delete()
    }
    // Also clear the actual SharedPreferences to be sure
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
}

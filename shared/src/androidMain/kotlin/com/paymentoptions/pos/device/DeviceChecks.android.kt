package com.paymentoptions.pos.device

import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import com.paymentoptions.pos.currentActivity

actual fun getNfcStatus(): Pair<Boolean, Boolean> {
    val nfcAdapter = currentActivity?.let { NfcAdapter.getDefaultAdapter(it) }
    return Pair(
        nfcAdapter != null,
        nfcAdapter?.isEnabled == true,
    )
}

actual fun isDeveloperOptionsEnabled(): Boolean = false

actual fun openDevelopmentSettings() {
    currentActivity?.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
}

actual fun openNfcSettings() {
    currentActivity?.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
}

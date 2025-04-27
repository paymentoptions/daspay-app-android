package com.paymentoptions.pos.device

import android.content.Context
import android.nfc.NfcAdapter

class Nfc {
    companion object {
        fun getStatus(context: Context): Pair<Boolean, Boolean> {
            val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
            return Pair(
                nfcAdapter != null, // Is NFC supported?
                nfcAdapter?.isEnabled == true // Is NFC enabled?
            )
        }
    }
}
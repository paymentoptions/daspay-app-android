package com.paymentoptions.pos.device

import android.content.Context

class DeveloperOptions {
    companion object {
        fun isEnabled(context: Context): Boolean {
            return try {
                android.provider.Settings.Global.getInt(
                    context.contentResolver,
                    android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                    0
                ) == 1
            } catch (e: Exception) {
                false
            }
        }
    }
}
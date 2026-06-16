package com.paymentoptions.pos.device

actual fun getNfcStatus(): Pair<Boolean, Boolean> = Pair(false, false)

actual fun isDeveloperOptionsEnabled(): Boolean = false

actual fun openDevelopmentSettings() = Unit

actual fun openNfcSettings() = Unit

package com.paymentoptions.pos.device

/** @return Pair(isSupported, isEnabled) */
expect fun getNfcStatus(): Pair<Boolean, Boolean>

expect fun isDeveloperOptionsEnabled(): Boolean

expect fun openDevelopmentSettings()

expect fun openNfcSettings()

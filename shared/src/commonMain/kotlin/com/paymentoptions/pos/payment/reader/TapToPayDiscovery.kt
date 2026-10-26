package com.paymentoptions.pos.payment.reader

/**
 * Apple's native "how to use Tap to Pay on iPhone" content (`ProximityReaderDiscovery`, iOS 18+).
 * Covers the merchant-education requirements (4.5/4.6/4.7 — contactless, Apple Pay/wallets, PIN
 * entry) largely automatically, so no bespoke education screens are built for those here.
 *
 * Hidden/no-op on Android, which has no equivalent system content.
 */

/** True when native Tap to Pay education content can actually be shown on this platform. */
expect fun isTapToPayEducationAvailable(): Boolean

/** Presents it, if [isTapToPayEducationAvailable]; otherwise a no-op. */
expect suspend fun showTapToPayEducation()

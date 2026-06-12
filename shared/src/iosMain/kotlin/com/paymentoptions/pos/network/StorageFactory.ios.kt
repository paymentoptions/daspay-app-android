//package com.paymentoptions.pos.storage
//
//import com.russhwolf.settings.NSUserDefaultsSettings
//import com.russhwolf.settings.Settings
//import platform.Foundation.NSUserDefaults
//
///**
// * Creates a [Settings] backed by [NSUserDefaults] on iOS.
// *
// * For truly sensitive keys (tokens, passwords) you would use a Keychain-backed
// * implementation.  For simplicity here we use NSUserDefaults; swap to a
// * `KeychainSettings` implementation when you integrate a Keychain wrapper library.
// */
//fun createSettings(): Settings {
//    return NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
//}

package com.paymentoptions.pos.payment.reader

import kotlinx.serialization.Serializable

/**
 * Per-merchant values the MineSec Headless SDK needs to initialize the Tap to Pay reader.
 *
 * TODO: `profileId`, `merchantBusinessName` and `merchantCategoryCode` have no backend source yet
 * (confirmed: absent from both the sign-in response and the device-configuration response) and are
 * populated as empty strings until a real endpoint supplies them.
 */
@Serializable
data class MerchantConfig(
    val profileId: String,
    val merchantBusinessName: String,
    val merchantId: String,
    val merchantCategoryCode: String,
    val terminalEmvProfileId: String,
)

package com.paymentoptions.pos.utils

import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.AquirerResponse
import com.paymentoptions.pos.services.apiService.PaymentDetailsResponseData


/**
 * Retrieves the gateway note/remark, prioritizing the one from the acquirer response.
 * Returns a trimmed string, or an empty string if no notes are found.
 */
fun getGatewayNotes(
    paymentDetails: PaymentDetailsResponseData?,
    acquirerResponse: AquirerResponse? // Should ideally be AcquirerResponse
): String {
    // 1. Try notes from the SDK/Acquirer specific response
    if (acquirerResponse?.gatewayNotes?.isNotBlank() == true) {
        AppLogger.debug("Gateway Notes (from Acquirer): ${acquirerResponse.gatewayNotes}")
        return acquirerResponse.gatewayNotes.trim()
    }

    // 2. Fallback to the top-level Referenceremark field
    if (paymentDetails?.Referenceremark?.isNotBlank() == true) {
        AppLogger.debug("Gateway Notes (from Referenceremark): ${paymentDetails.Referenceremark}")
        return paymentDetails.Referenceremark.trim()
    }

    AppLogger.debug("Gateway Notes Referenceremark :$paymentDetails and  ${paymentDetails?.Referenceremark}  and acquires : $acquirerResponse")
    return ""
}
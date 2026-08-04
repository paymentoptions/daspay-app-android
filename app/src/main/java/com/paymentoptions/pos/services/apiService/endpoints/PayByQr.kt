package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.device.DPSharedPreferences
import com.paymentoptions.pos.services.apiService.PayByLinkRequest
import com.paymentoptions.pos.services.apiService.PayByLinkResponse


suspend fun payByQr(
    context: Context,
    payByQrRequest: PayByLinkRequest,
): PayByLinkResponse? {
    val qrDasmid = DPSharedPreferences.getQRDasmid(context)

    // Call underlying payByLink function with the correct DASMID
    return payByLink(context, payByQrRequest, qrDasmid)
}
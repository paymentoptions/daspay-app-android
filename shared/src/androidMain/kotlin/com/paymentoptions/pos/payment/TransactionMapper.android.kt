package com.paymentoptions.pos.payment

import com.paymentoptions.pos.network.PaymentStatusRequest
import com.theminesec.lib.dto.transaction.Transaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

fun Transaction.toPaymentStatusRequest(
    parentUUID: String? = null,
    childUUID: String? = null,
    gateWayNotes: String? = null
): PaymentStatusRequest {

    val transactionJson = JSONObject(Json.encodeToString(this))
    if (transactionJson.has("description")) {
        val descriptionValue = transactionJson.opt("description")
        transactionJson.remove("description")
        transactionJson.put("gatewayNotes", descriptionValue)
    }

    if(gateWayNotes?.isNotBlank() == true){
        transactionJson.put("gatewayNotes", gateWayNotes.trim())
    }

    return PaymentStatusRequest(
        tranId = this.posReference.toString(),
        cvmPerformed = this.cvmPerformed.toString(),
        tsi = this.tsi.toString(),
        mcc = this.mcc,
        merchantName = this.merchantName,
        tranStatus = this.tranStatus.toString(),
        tranType = this.tranType.toString(),
        atc = this.atc.toString(),
        createdAt = this.createdAt.toEpochMilliseconds().toString(),
        updatedAt = this.updatedAt?.toEpochMilliseconds().toString(),
        trace = this.trace,
        callbackUrl = this.callbackUrl.toString(),
        entryMode = this.entryMode.toString(),
        amount = "{\"currency\":\"${this.amount.currency}\",\"value\":${this.amount.value.toFloat()}",
        batchNo = this.batchNo.toString(),
        appName = this.appName.toString(),
        linkedTranId = this.posReference.toString(),
        merchantAddr = this.merchantAddr.toString(),
        rrn = this.rrn.toString(),
        tc = this.tc.toString(),
        tvr = this.tvr.toString(),
        accountMasked = this.accountMasked.toString(),
        sdkId = this.sdkId.toString(),
        paymentMethod = this.paymentMethod.toString(),
        hostMessageFormat = this.hostMessageFormat.toString(),
        aid = this.aid.toString(),
        acquirerResponse = transactionJson.toString(),
        parentUUID = parentUUID,
        childUUID = childUUID,
    )
}

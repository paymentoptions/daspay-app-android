package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.UploadSignatureResponse

actual suspend fun uploadSignature(
    signatureBytes: ByteArray,
    transactionId: String
): UploadSignatureResponse? {
    TODO("Not yet implemented")
}
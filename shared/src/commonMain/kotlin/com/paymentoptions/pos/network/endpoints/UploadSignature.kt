package com.paymentoptions.pos.network.endpoints

import com.paymentoptions.pos.network.UploadSignatureResponse

expect suspend fun uploadSignature(signatureBytes: ByteArray, transactionId: String): UploadSignatureResponse?



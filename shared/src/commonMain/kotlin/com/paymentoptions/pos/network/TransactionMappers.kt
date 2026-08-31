package com.paymentoptions.pos.network

fun InsightsResponseDataRecord.toTransactionListDataRecord(): TransactionListDataRecord {
    val currencyCode = CurrencyCode ?: ""
    val transactionStatus = status ?: ""
    val productType = ProductType ?: ""
    val transactionDate = TransactionDate ?: ""
    val terminalId = TerminalID ?: ""
    val transactionId = ID?.toIntOrNull() ?: 0
    val dasmid = DASMID ?: ""

    return TransactionListDataRecord(
        uuid = uuid,
        V2UUID = uuid,
        TransactionType = TransactionType,
        amount = amount.toString(),
        CurrencyCode = currencyCode,
        status = transactionStatus,
        PaymentType = paymentMethod,
        ProductType = productType,
        Date = transactionDate,
        UpdatedDate = transactionDate,
        TerminalId = terminalId,
        TerminalName = terminalId,
        TransactionID = transactionId,
        SettleStatus = SettleStatus,
        BatchID = BatchID,
        BatchNo = BatchNo,
        SettledAt = "N/A",
        AcquirerTransactionID = AcquirerTransactionID,
        DASMID = dasmid,
        IsVoided = IsVoided,
        IsRefunded = IsRefunded,
        Isrecurring = false,
        IsWhitelisted = false,
        MerchantRefID = "",
        LegalName = "",
        LegalNameInEnglish = "",
        trackID = "",
        AcquirerMID = "",
        Scheme = "",
        CardNumber = "",
        AcquirerCode = "",
        AuthCode = "",
        SubscriptionId = "",
        PBLLinkName = "N/A",
        GatewayResponse = "",
        ResponseCode = "",
        IntegrationType = "",
        has3DS = false,
    )
}

fun PaymentDetailsResponseData.toTransactionListDataRecord(): TransactionListDataRecord {
    return TransactionListDataRecord(
        uuid = this.TransactionRefID, // Assuming TransactionRefID maps to uuid
        MerchantRefID = this.MerchantRefNumber ?: this.MerchantID,
        LegalName = this.LegalNameInEnglish ?: this.Merchant ?: "",
        LegalNameInEnglish = this.LegalNameInEnglish,
        DASMID = this.DASMID,
        trackID = this.trackID,
        AcquirerMID = this.AcquirerMID ?: "",
        TransactionType = this.TransactionType,
        Scheme = this.Scheme,
        amount = this.Amount.toString(), // Float to String
        CurrencyCode = this.CurrencyCode,
        status = this.Status,
        CardNumber = this.CardNumber,
        AcquirerCode = this.AcquirerCode,
        has3DS = false, // Defaulting to false as it's missing in source
        AuthCode = this.AuthCode,
        Isrecurring = this.Isrecurring,
        Date = this.Date,
        V2UUID = this.V2UUID,
        ProductType = this.ProductType ?: "",
        SubscriptionId = this.SubscriptionDetails,
        UpdatedDate = this.UpdatedDate,
        TerminalId = this.TerminalID ?: "",
        IsWhitelisted = false, // Defaulting to false as it's missing in source
        GatewayResponse = this.GatewayError ?: this.ACQError,
        ResponseCode = this.Response.toString(), // Int to String
        TransactionID = this.TransactionID.toInt(), // String to Int safely
        IntegrationType = "", // Missing in source, providing empty string
        PaymentType = this.PaymentType,
        SettleStatus = null, // Missing in source
        BatchID = null, // Missing in source
        BatchNo = null, // Missing in source
        SettledAt = null, // Missing in source
        AcquirerTransactionID = this.AcquirerReferenceNumber,
        IsVoided = null, // Missing in source
        IsRefunded = null ,// Missing in source
        TerminalName = "",
        PBLLinkName = ""
    )
}

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

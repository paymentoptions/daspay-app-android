package com.paymentoptions.pos.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

// ── Auth / Token ──────────────────────────────────────────────────────────────

@Serializable
data class Token(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String,
)

@Serializable
data class SignInData(
    val token: Token,
    val email: String = "",
    val exp: Long = 0L,
    val uid: String = "",
    val auth_time: Long = 0L,
    val Groups: List<String> = emptyList(),
    val subsidiaries: List<String> = emptyList(),
    val name: String = "",
    val appLevel: String = "",
    val contactNo: String = "",
    val referralCode: String = "",
    val accessLevel: AccessLevel? = null,
    val signInAsMerchant: Boolean = false,
    val passwordExpiry: String = "",
)

@Serializable
enum class AccessLevel { ADMIN, STAFF }

@Serializable
data class SignInRequest(
    val username: String,
    val password: String,
)

@Serializable
data class SignInResponse(
    val statusCode: Int? = null,
    val message: String? = null,
    val messageCode: String? = null,
    val success: Boolean? = null,
    val data: SignInData? = null,
)

@Serializable
data class SignOutRequest(
    val username: String,
    val token: Token,
)

@Serializable
data class SignOutResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val messageCode: String,
    val data: String,
)

@Serializable
data class RefreshTokenRequest(
    val username: String,
    val refreshToken: String,
)

// ── Transaction List ──────────────────────────────────────────────────────────

@Serializable
data class TransactionListV2RequestFilter(
    val field: String,
    val operator: String,
    val value: String,
    val operand: String? = null,
)

@Serializable
data class TransactionListV2Request(
    val take: Int,
    val skip: Int,
    val totalRequired: Boolean = true,
    val TimeZone: String = "Indian/Mahe",
    val filter: List<TransactionListV2RequestFilter>,
)

@Serializable
data class TransactionListDataRecord(
    val uuid: String,
    val MerchantRefID: String,
    val LegalName: String,
    val LegalNameInEnglish: String? = null,
    val DASMID: String,
    val trackID: String? = null,
    val AcquirerMID: String,
    val TransactionType: String,
    val Scheme: String,
    val amount: String,
    val CurrencyCode: String,
    val status: String,
    val CardNumber: String,
    val AcquirerCode: String,
    val has3DS: Boolean,
    val AuthCode: String? = null,
    val Isrecurring: Boolean,
    val Date: String,
    val V2UUID: String? = null,
    val ProductType: String,
    val SubscriptionId: String? = null,
    val UpdatedDate: String,
    val TerminalId: String,
    val TerminalName: String,
    val PBLLinkName: String? = null,
    val IsWhitelisted: Boolean,
    val GatewayResponse: String? = null,
    val ResponseCode: String? = null,
    val TransactionID: Int,
    val IntegrationType: String,
    val PaymentType: String? = null,
    val SettleStatus: String? = null,
    val BatchID: String? = null,
    val BatchNo: String? = null,
    val SettledAt: String? = null,
    val AcquirerTransactionID: String? = null,
    val IsVoided: Boolean? = null,
    val IsRefunded: Boolean? = null,
)

@Serializable
data class TransactionRequest(
    val transactionId: String,
    val merchant_id: String,
    val amount: String? = null,
    val notes: String? = null,
)

@Serializable
data class TransactionListData(
    val total_count: Int = 0,
    val total_amount: String = "0",
    val records: List<TransactionListDataRecord?> = emptyList(),
    val total_sales: String? = null,
    val total_refund: String? = null,
    val approval_ratio: String? = null,
    val decline_count: String? = null,
)

@Serializable
data class TransactionListResponse(
    val statusCode: Int = 0,
    val message: String = "",
    val messageCode: String = "",
    val success: Boolean = false,
    val data: TransactionListData = TransactionListData(),
)

// ── Refund ────────────────────────────────────────────────────────────────────

@Serializable
data class GatewayResponse(
    val version: String,
    val type: String,
    val message: String,
    val code: String,
)

@Serializable
data class Refund_MerchantDetails(val mid: String = "")

@Serializable
data class RefundPaymentDetailsCard(
    val name: String = "",
    val number: String = "",
    val exp_month: String = "",
    val exp_year: String = "",
)

@Serializable
data class Refund_PaymentDetails_AdditionalData_PaymentDataSource(val type: String = "")

@Serializable
data class Refund_PaymentDetails_AdditionalData(
    val payment_data_source: Refund_PaymentDetails_AdditionalData_PaymentDataSource =
        Refund_PaymentDetails_AdditionalData_PaymentDataSource(),
)

@Serializable
data class Refund_PaymentDetails(
    val amount: Float = 0f,
    val response_code: Int = 0,
    val auth_code: String = "",
    val currency: String = "",
    val payment_method: String = "",
    val scheme: String = "",
    val card: RefundPaymentDetailsCard = RefundPaymentDetailsCard(),
    val additional_data: Refund_PaymentDetails_AdditionalData = Refund_PaymentDetails_AdditionalData(),
)

@Serializable
data class Refund_TransactionDetails(
    val id: String = "",
    val ref: Int = 0,
    val timestamp: String = "",
    val merchant_txn_ref: String = "",
)

@Serializable
data class RefundResponse(
    val success: Boolean = false,
    val status_code: Int = 0,
    val is_live: Boolean = false,
    val transaction_type: String = "",
    val gateway_response: GatewayResponse = GatewayResponse("", "", "", ""),
    val merchant_details: Refund_MerchantDetails = Refund_MerchantDetails(),
    val payment_details: Refund_PaymentDetails = Refund_PaymentDetails(),
    val transaction_details: Refund_TransactionDetails = Refund_TransactionDetails(),
)

// ── Payment ───────────────────────────────────────────────────────────────────

@Serializable
data class PaymentReturnUrl(
    val webhook_url: String = "",
    val success_url: String = "",
    val decline_url: String = "",
)

@Serializable
data class Address(
    val country: String = "",
    val email: String = "",
    val address1: String = "",
    val phone_number: String = "",
    val city: String = "",
    val state: String = "",
    val postal_code: String = "",
)

@Serializable
data class PaymentMethodRequest(val type: String = "")

@Serializable
data class PaymentRequest(
    val amount: String,
    val currency: String,
    val merchant_txn_ref: String,
    val customer_ip: String,
    val merchant_id: String,
    val return_url: PaymentReturnUrl,
    val billing_address: Address,
    val shipping_address: Address,
    val payment_method: PaymentMethodRequest,
    val time_zone: String,
)

@Serializable
data class Card(
    val name: String = "",
    val number: String = "",
    val exp_month: String = "",
    val exp_year: String = "",
)

@Serializable
data class PaymentDetails(
    val amount: Float = 0f,
    val response_code: Int = 0,
    val responseDescription: String = "",
    val auth_code: String = "",
    val currency: String = "",
    val payment_method: String = "",
    val scheme: String = "",
    val card: Card = Card(),
)

@Serializable
data class BillingDetails(
    val billing_address: Address = Address(),
    val shipping_address: Address = Address(),
)

@Serializable
data class TransactionDetails(
    val id: String = "",
    val ref: Int = 0,
    val timestamp: String = "",
    val billing_details: BillingDetails = BillingDetails(),
)

@Serializable
data class RiskDetails(val risk_score: String = "")

@Serializable
data class PaymentResponse(
    val success: Boolean = false,
    val status_code: Int = 0,
    val is_live: Boolean = false,
    val transaction_type: String = "",
    val gateway_response: GatewayResponse = GatewayResponse("", "", "", ""),
    val payment_details: PaymentDetails = PaymentDetails(),
    val transaction_details: TransactionDetails = TransactionDetails(),
    val risk_details: RiskDetails = RiskDetails(),
)

// ── Payment Status ────────────────────────────────────────────────────────────

@Serializable
data class PaymentStatusRequest(
    val tranId: String? = null,
    val cvmPerformed: String,
    val tsi: String? = null,
    val mcc: String,
    val merchantName: String,
    val tranStatus: String,
    val tranType: String,
    val atc: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val trace: String,
    val callbackUrl: String? = null,
    val entryMode: String,
    val amount: String,
    val batchNo: String,
    val appName: String? = null,
    val linkedTranId: String? = null,
    val merchantAddr: String,
    val rrn: String? = null,
    val tc: String? = null,
    val tvr: String? = null,
    val accountMasked: String? = null,
    val sdkId: String? = null,
    val paymentMethod: String,
    val hostMessageFormat: String,
    val aid: String? = null,
    val acqMid: String = "null",
    val acqTid: String = "null",
    val notifyId: Int = 0,
    val acquirerResponse: String = "",
    val parentUUID: String? = null,
    val childUUID: String? = null,
)

// ── Pay By Link ───────────────────────────────────────────────────────────────

@Serializable
data class PayByLinkRequestProduct(
    val Currency: String,
    val Name: String,
    val Quantity: Int,
    val Price: Float,
    val TotalPrice: String,
)

@Serializable
data class PayByLinkRequest(
    val ExpiryDate: String,
    val PBLLinkName: String,
    val Product: List<PayByLinkRequestProduct>,
)

@Serializable
data class PayByLinkResponseDataProduct(
    val Id: String = "",
    val Name: String = "",
    val Price: Float = 0f,
    val Currency: String = "",
    val Quantity: Int = 0,
    val TotalPrice: String = "",
)

@Serializable
data class PayByLinkResponseData(
    val ProductID: String = "",
    val DASMID: String = "",
    val Product: List<PayByLinkResponseDataProduct> = emptyList(),
    val Amount: Float = 0f,
    val status: String = "",
    val CreatedAt: String = "",
    val UpdatedAt: String = "",
    val ExpiryDate: String = "",
    val ReminderDate: String = "",
    val IsCustomerStatus: Boolean = false,
    val Currency: String = "",
    val ReturnUrl: JsonObject = buildJsonObject { },
    val PBLLinkName: String = "",
    val ID: String = "",
)

@Serializable
data class PayByLinkResponse(
    val statusCode: Int = 0,
    val message: String = "",
    val messageCode: String = "",
    val success: Boolean = false,
    val data: PayByLinkResponseData = PayByLinkResponseData(),
)

// ── Product Categories ────────────────────────────────────────────────────────

@Serializable
data class CategoryListDataRecord(
    val CategoryName: String = "",
    val CategoryDesc: String = "",
    val CategoryStatus: String = "",
    val MerchantID: String = "",
    val CategoryID: String = "",
    val CreatedAt: String = "",
    val UpdatedAt: String? = null,
    val DeletedAt: String? = null,
    val DeletedBy: String? = null,
)

@Serializable
data class CategoryListData(
    val total_count: Int = 0,
    val records: List<CategoryListDataRecord> = emptyList(),
)

@Serializable
data class CategoryListResponse(
    val statusCode: Int = 0,
    val message: String = "",
    val messageCode: String = "",
    val success: Boolean = false,
    val data: CategoryListData = CategoryListData(),
)

// ── Products ──────────────────────────────────────────────────────────────────

@Serializable
data class ProductListDataRecord(
    val CategoryID: String = "",
    val ProductName: String = "",
    val ProductDesc: String = "",
    val ProductStatus: Boolean = false,
    val ProductPrice: Float = 0f,
    val ProductID: String = "",
    val ProductImage: String? = null,
    val ProductCode: String = "",
    val ProductFoodType: String? = null,
    val ProductSize: String? = null,
    val CreatedAt: String? = null,
    val UpdatedAt: String? = null,
    val ProductStock: Int? = 0,
    val MerchantID: String = "",
    val Currency: String = "",
    val DeletedAt: String? = null,
    val DeletedBy: String? = null,
)

@Serializable
data class ProductRequest(
    val ProductName: String? = null,
    val ProductDesc: String? = null,
    val ProductPrice: Float? = null,
    val ProductFoodType: String? = null,
    val ProductSize: String? = null,
    val ProductCode: String? = null,
    val ProductStatus: Boolean? = null,
    val ProductStock: Long? = null,
    val Currency: String? = null,
    val MerchantID: String? = null,
    val CategoryID: String? = null,
)

@Serializable
data class ProductListResponseData(
    val total_count: Int = 0,
    val records: List<ProductListDataRecord> = emptyList(),
)

@Serializable
data class ProductListResponse(
    val statusCode: Int = 0,
    val message: String = "",
    val messageCode: String = "",
    val success: Boolean = false,
    val data: ProductListResponseData = ProductListResponseData(),
)

@Serializable
data class GetSignatureResponse (
    val statusCode: Long,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: SignatureData
)

@Serializable
data class SignatureData (
    val imageExists: Boolean,
    val signatureURL: String? = null
)

@Serializable
data class ProductResponse(
    val statusCode: Long = 0,
    val message: String = "",
    val messageCode: String = "",
    val success: Boolean = false,
    val data: ProductListDataRecord = ProductListDataRecord(),
)

// ── Device Registration ───────────────────────────────────────────────────────

@Serializable
data class DeviceMetadata(
    val os: String = "",
    val version: String = "",
    val manufacturer: String = "",
)

@Serializable
data class DeviceInfo(
    val DeviceID: String = "",
    val DeviceNumber: String = "",
    val DASMID: String = "",
    val DeviceMetadata: DeviceMetadata = DeviceMetadata(),
    val DeviceName: String = "",
    val DeviceType: String = "",
    val Status: String = "",
    val LastUsedAt: String = "",
    val CreatedAt: String = "",
    val UpdatedAt: String = "",
    val Location: String? = null,
)

@Serializable
data class DevicePaymentMethod_Schemes(
    val hasVISA: Boolean = false,
    val hasMastercard: Boolean = false,
    val hasJCB: Boolean = false,
    val hasAmex: Boolean = false,
    val hasUnionPay: Boolean = false,
)

@Serializable
data class DevicePaymentMethod_Apms(
    val hasAlipay: Boolean = false,
    val hasWechatpay: Boolean = false,
    val hasApplePay: Boolean = false,
    val hasGooglePay: Boolean = false,
    val hasDinersClub: Boolean = false,
    val hasGCash: Boolean = false,
    val hasPayPay: Boolean = false,
    val hasKonbini: Boolean = false,
    val hasPayEasy: Boolean = false,
)

@Serializable
data class DevicePaymentMethod(
    val DASMID: String = "",
    val TransactionCCY: List<String> = emptyList(),
    val SettlementCCY: String = "",
    val schemes: DevicePaymentMethod_Schemes = DevicePaymentMethod_Schemes(),
    val apms: DevicePaymentMethod_Apms = DevicePaymentMethod_Apms(),
    val Type: String = "",
    val Status: String = "",
)

@Serializable
data class ExternalConfigData(
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val paymentMethod: List<DevicePaymentMethod> = emptyList(),
)

@Serializable
data class ExternalConfigurationResponse(
    val status: Int = 0,
    val message: String = "",
    val messageCode: String = "",
    val success: Boolean = false,
    val data: ExternalConfigData? = null,
)

@Serializable
data class CompleteDeviceRegistrationRequest(
    val UniqueCode: String,
    val DeviceNumber: String,
    val DeviceMetadata: DeviceMetadata,
    val DeviceType: String = "MOBILE",
)

@Serializable
data class CompleteDeviceRegistrationResponse(
    val status: Int = 0,
    val message: String = "",
    val messageCode: String = "",
    val success: Boolean = false,
)

// ── Insights ──────────────────────────────────────────────────────────────────

@Serializable
data class InsightsResponseDataRecord(
    val uuid: String,
    val status: String? = null,
    val TerminalID: String? = null,
    val ID: String? = null,
    val amount: Float,
    val CurrencyCode: String? = null,
    val TransactionDate: String? = null,
    val paymentMethod: String? = null,
    val event: String? = null,
    val TransactionType: String,
    val DASMID: String? = null,
    val AcquirerTransactionID: String? = null,
    val BatchID: String? = null,
    val BatchNo: String? = null,
    val SettleStatus: String? = null,
    val UpdatedAt: String? = null,
    val ProductType: String? = null,
    val IsVoided: Boolean? = null,
    val IsRefunded: Boolean? = null,
)

@Serializable
data class InsightsResponseData(
    val total: Int,
    val skip: Int,
    val take: Int,
    val records: List<InsightsResponseDataRecord>,
)

@Serializable
data class InsightsResponse(
    val statusCode: Int,
    val message: String? = null,
    val messageCode: String? = null,
    val success: Boolean,
    val data: InsightsResponseData,
)

// ── Stats V2 ──────────────────────────────────────────────────────────────────

@Serializable
data class StatsV2Request(
    val TimeZone: String,
    val Currency: String,
)

@Serializable
data class StatsV2Response(val dummy: String = "")

// ── Payment Details ───────────────────────────────────────────────────────────

@Serializable
data class PrimaryAddress(
    val Line1: String? = null,
    val Line2: String? = null,
    val Line3: String? = null,
    val Line4: String? = null,
    val Locality: String? = null,
    val Region: String? = null,
    val PostCode: String? = null,
    val Country: String? = null,
)

@Serializable
data class PaymentDetailsResponseData(
    val TransactionRefID: String,
    val V2UUID: String? = null,
    val MerchantID: String,
    val Isrecurring: Boolean,
    val TransactionType: String,
    val Date: String,
    val UpdatedDate: String,
    val Amount: Double,
    val DASMID: String,
    val CurrencyCode: String,
    val Response: Int? = null,
    val trackID: String,
    val Status: String,
    val AuthCode: String? = null,
    val CardHolder: String,
    val CVVResponse: String,
    val ExpiryDate: String,
    val Memo: String? = null,
    val AcquirerReferenceNumber: String,
    val Scheme: String,
    val TransactionID: Long,
    val CardNumber: String,
    val EmailAddress: String,
    val Phone: String,
    val BillingPostcode: String,
    val BillingCountry: String,
    val BillingAddress: String,
    val BillingCity: String,
    val ShippingAddress: String,
    val ShippingCountry: String,
    val ShippingCity: String,
    val ShippingPostcode: String,
    val CustomerIP: String,
    val MerchantIP: String,
    val AcquirerCode: String,
    val AcquirerMID: String,
    val Event: String,
    val ACQError: String,
    val GatewayError: String,
    val BIN: String,
    val IssuingBank: String,
    val IssuingCountry: String,
    val MerchantRefNumber: String,
    val TransactionTimezone: String,
    val AcquirerID: String,
    val TerminalID: String,
    val browser_info: String? = null,
    val MerchantCategoryCode: String,
    val ProductType: String,
    val PrimaryAddress: PrimaryAddress? = null,
    val Merchant: String,
    val Referenceremark: String,
    val LegalNameInEnglish: String,
    val SecretKey: String,
    val SubscriptionDetails: String? = null,
    val PaymentType: String,
    val AcquirerResponse: List<String>? = emptyList(),
)

@Serializable
data class PaymentDetailsResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: PaymentDetailsResponseData,
)

// ── Acquirer Response ─────────────────────────────────────────────────────────

@Serializable
data class AquirerResponseAmount(
    val value: String = "",
    val currency: String = "",
)

@Serializable
data class AquirerResponseAction(
    val actionId: String = "",
    val trace: String = "",
    val actionType: String = "",
    val actionStatus: String = "",
    val requestId: String = "",
    val amount: AquirerResponseAmount = AquirerResponseAmount(),
    val tranId: String = "",
    val reason: String = "",
    val hostRespCode: String = "",
    val hostRespMessage: String = "",
    val posReference: String = "",
    val extraData: String = "",
    val createdAt: String = "",
)

@Serializable
data class AquirerResponse(
    val tranId: String = "",
    val tranType: String = "",
    val tranStatus: String = "",
    val amount: AquirerResponseAmount = AquirerResponseAmount(),
    val paymentMethod: String = "",
    val entryMode: String = "",
    val accountMasked: String = "",
    val accountBin: String = "",
    val accountLast4: String = "",
    val issCountryCode: String = "",
    val cvmPerformed: String? = null,
    val aid: String = "",
    val appName: String = "",
    val hostRespMessage: String = "",
    val tc: String = "",
    val tvr: String = "",
    val tsi: String = "",
    val atc: String = "",
    val profileId: String = "",
    val acceptanceId: String = "",
    val acptId: String = "",
    val sdkId: String = "",
    val posReference: String = "",
    val trace: String = "",
    val merchantName: String = "",
    val merchantAddr: String = "",
    val mcc: String = "",
    val primaryMid: String = "",
    val primaryTid: String = "",
    val hostMessageFormat: String = "",
    val providerReference: String = "",
    val providerMchId: String = "",
    val extraData: String = "",
    val rrn: String = "",
    val approvalCode: String = "",
    val batchId: String = "",
    val batchNo: String = "",
    val actions: List<AquirerResponseAction> = listOf(AquirerResponseAction()),
    val srsTranId: String = "",
    val consumerPaymentDevice: String = "",
    val createdAt: String = "",
)

// ── Upload / Media ────────────────────────────────────────────────────────────

@Serializable
data class UploadSignatureResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
)

@Serializable
data class ProductImageRequest(
    val ProductID: String,
    val fileName: String,
)

@Serializable
data class UploadImageData(
    val signedUrl: String,
    val fileName: String,
)

@Serializable
data class UploadImageResponse(
    val statusCode: Long,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: UploadImageData,
)

// ── Settlement ────────────────────────────────────────────────────────────────

@Serializable
data class SettleBatchRequest(val batchId: String)

@Serializable
data class SettleBatchResponse(
    val status_code: Long,
    val BatchID: String,
    val SettleStatus: String,
    val SettlementRes: String,
)

@Serializable
data class SettlementRecord(
    val ID: Long = 0,
    val uuid: String = "",
    val BatchID: String = "",
    val BatchNo: String = "",
    val SettleStatus: String = "",
    val CreatedAt: String = "",
    val UpdatedAt: String? = null,
    val SettledAt: String? = null,
    val Capture: Long = 0,
    val CaptureAmount: Double = 0.0,
    val Sale: Long = 0,
    val SaleAmount: Double = 0.0,
    val Refund: Long = 0,
    val RefundAmount: Double = 0.0,
    val Voided: Long = 0,
    val VoidedAmount: Double = 0.0,
)

@Serializable
data class SettlementData(
    val totalCount: Long = 0,
    val take: Long = 0,
    val skip: Long = 0,
    val records: List<SettlementRecord> = emptyList(),
)

@Serializable
data class SettlementListResponse(
    val statusCode: Long = 0,
    val success: Boolean = false,
    val message: String = "",
    val data: SettlementData = SettlementData(),
)

// ── App Config ────────────────────────────────────────────────────────────────

@Serializable
data class AppConfig(
    val ID: Long,
    val AppENV: String,
    val BaseAPIURL: String,
    val RegistryLogin: String,
    val RegistryToken: String,
    val PrevAppVersion: Long,
    val CurrAppVersion: Long,
    val IsUpdateMandatory: Boolean,
    val TransactionDetailsURL: String,
)

@Serializable
data class AppConfigResponse(
    val statusCode: Long,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: List<AppConfig>,
)

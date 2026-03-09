package com.paymentoptions.pos.services.apiService

import com.theminesec.lib.dto.transaction.Transaction
import kotlinx.serialization.Serializable
import org.json.JSONObject
import kotlin.String

@Serializable
data class Token(
    val accessToken: String,
    val idToken: String,
    val refreshToken: String,
)

@Serializable
data class SignInData(
    val token: Token,
    val email: String,
    val exp: Long,
    val uid: String,
    val auth_time: Long,
    val Groups: List<String>,
    val subsidiaries: List<String>,
    val name: String,
    val appLevel: String,
    val contactNo: String,
    val referralCode: String,
    val accessLevel: AccessLevel,
    val signInAsMerchant: Boolean,
    val passwordExpiry: String,
)

@Serializable
enum class AccessLevel{
    ADMIN, STAFF
}

data class SignInRequest(
    val username: String,
    val password: String,
)

@Serializable
data class SignInResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: SignInData,
)

data class SignOutRequest(
    val username: String,
    val token: Token,
)

data class SignOutResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val messageCode: String,
    val data: String,
)

data class RefreshTokenRequest(
    val username: String,
    val refreshToken: String,
)

// Transaction List Models ------------------------------------
data class TransactionListV2RequestFilter(
    val field: String,
    val operator: String,
    val value: String,
)

data class TransactionListV2Request(
    val take: Int,
    val skip: Int,
    val totalRequired: Boolean = true,
    val TimeZone: String = "Indian/Mahe",
    val filter: List<TransactionListV2RequestFilter>,
)

data class TransactionListDataRecord(
    val uuid: String,
    val MerchantRefID: String,
    val LegalName: String,
    val LegalNameInEnglish: String?,
    val DASMID: String,
    val trackID: String?,
    val AcquirerMID: String,
    val TransactionType: String,
    val Scheme: String,
    val amount: String,
    val CurrencyCode: String,
    val status: String,
    val CardNumber: String,
    val AcquirerCode: String,
    val has3DS: Boolean,
    val AuthCode: String?,
    val Isrecurring: Boolean,
    val Date: String,
    val V2UUID: String?,
    val ProductType: String,
    val SubscriptionId: String?,
    val UpdatedDate: String,
    val TerminalId: String,
    val TerminalName: String,
    val PBLLinkName: String?,
    val IsWhitelisted: Boolean,
    val GatewayResponse: String?,
    val ResponseCode: String?,
    val TransactionID: Int,
    val IntegrationType: String,
    val PaymentType: String?,
    val SettleStatus: String?,
    val BatchID: String?,
    val BatchNo: String?,
    val SettledAt: String?,
    val AcquirerTransactionID: String?,
    val IsVoided: Boolean?,
    val IsRefunded: Boolean?,
)


data class TransactionRequest(
    val transactionId: String,
    val merchant_id: String,
    val amount: String? = null,
    val notes: String? = null,
    val daspay_res: Transaction? = null,
)


data class TransactionListData(
    val total_count: Int,
    val total_amount: String,
    val records: List<TransactionListDataRecord?>,

    // Optional but useful
    val total_sales: String? = null,
    val total_refund: String? = null,
    val approval_ratio: String? = null,
    val decline_count: String? = null
)


data class TransactionListResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: TransactionListData,
)
// -------------------------------------------------------

data class RefundRequest(
    val id: String,
    val merchant_id: String,
    val refundAmount: Int,
)

data class GatewayResponse(
    val version: String,
    val type: String,
    val message: String,
    val code: String,
)

data class Refund_MerchantDetails(
    //"legal_name": "Kavi Kokonut Kafe Co. Ltd",
    //"merchant_txn_ref": "1234",
    //"billing_details": {
    //    "billing_address": {
    //        "country": "JP",
    //        "email": "diksha@gmail.com",
    //        "address1": "Chiyoda1-1",
    //        "phone_number": "",
    //        "phone": "",
    //        "city": "Minatoku",
    //        "state": "Tokyoto",
    //        "postal_code": "1000001"
    //    },
    //    "shipping_address": {
    //        "country": "JP",
    //        "email": "diksha@gmail.com",
    //        "address1": "Chiyoda1-1",
    //        "phone_number": "",
    //        "phone": "",
    //        "city": "Minatoku",
    //        "state": "Tokyoto",
    //        "postal_code": "1000001"
    //    }
    //},
    //"device_details": {
    //    "visited_ip": "",
    //    "merchant_ip": "",
    //    "customer_ip": ""
    val mid: String,
)

data class RefundPaymentDetailsCard(
    val name: String,
    val number: String,
    val exp_month: String,
    val exp_year: String,
)

data class Refund_PaymentDetails_AdditionalData_PaymentDataSource(
    val type: String,
)

data class Refund_PaymentDetails_AdditionalData(
    val payment_data_source: Refund_PaymentDetails_AdditionalData_PaymentDataSource,
)

data class Refund_PaymentDetails(
    val amount: Float,
    val response_code: Int,
    val auth_code: String,
    val currency: String,
    val payment_method: String,
    val scheme: String,
    val card: RefundPaymentDetailsCard,
    val additional_data: Refund_PaymentDetails_AdditionalData,
)

data class Refund_TransactionDetails(
    val id: String,
    val ref: Int,
    val timestamp: String,
    val merchant_txn_ref: String,
)

data class RefundResponse (
    val success: Boolean,
    val status_code: Int,
    val is_live: Boolean,
    val transaction_type: String,
    val gateway_response: GatewayResponse,
    val merchant_details: Refund_MerchantDetails,
    val payment_details: Refund_PaymentDetails,
    val transaction_details: Refund_TransactionDetails,
)

// Payment API Models ------------------------------------
data class PaymentReturnUrl(
    val webhook_url: String,
    val success_url: String,
    val decline_url: String,
)

data class Address(
    val country: String,
    val email: String,
    val address1: String,
    val phone_number: String,
    val city: String,
    val state: String,
    val postal_code: String,
)

data class PaymentMethod(
    val type: String,
)

data class PaymentRequest(
    val amount: String,
    val currency: String,
    val merchant_txn_ref: String,
    val customer_ip: String,
    val merchant_id: String,
    val return_url: PaymentReturnUrl,
    val billing_address: Address,
    val shipping_address: Address,
    val payment_method: PaymentMethod,
    val time_zone: String,
)

data class Card(
    val name: String,
    val number: String,
    val exp_month: String,
    val exp_year: String,
    //val additional_data: TODO
)

data class PaymentDetails(
    val amount: Float,
    val response_code: Int,
    val responseDescription: String,
    val auth_code: String,
    val currency: String,
    val payment_method: String,
    val scheme: String,
    val card: Card,
)

data class TransactionDetails(
    val id: String,
    val ref: Int,
    val timestamp: String,
    val billing_details: BillingDetails,
)

data class BillingDetails(
    val billing_address: Address,
    val shipping_address: Address,
)

data class RiskDetails(
    val risk_score: String,
)

data class PaymentResponse(
    val success: Boolean,
    val status_code: Int,
    val is_live: Boolean,
    val transaction_type: String,
    val gateway_response: GatewayResponse,
//    val merchant_details :    TODO
    val payment_details: PaymentDetails,
    val transaction_details: TransactionDetails,
    val risk_details: RiskDetails,
)
// -------------------------------------------------------

// Payment status related ----------------------------------

@Serializable
data class PaymentStatusAmount(
    val currency: String,
    val value: Float,
)

data class PaymentStatusRequest(
    val tranId: String?,
    val cvmPerformed: String,
    val tsi: String?,
    val mcc: String,
    val merchantName: String,
    val tranStatus: String,
    val tranType: String,
    val atc: String?,
    val createdAt: String,
    val updatedAt: String,
    val trace: String,
    val callbackUrl: String?,
    val entryMode: String,
    val amount: String,
    val batchNo: String,
    val appName: String?,
    val linkedTranId: String?,
    val merchantAddr: String,
    val rrn: String?,
    val tc: String?,
    val tvr: String?,
    val accountMasked: String?,
    val sdkId: String?,
    val paymentMethod: String,
    val hostMessageFormat: String,
    val aid: String?,
    val acqMid: String = "null",
    val acqTid: String = "null",
    val notifyId: Int = 0,
    val acquirerResponse: String = "",
)

data class PaymentStatusResponseData(
    val foo: String,
)

data class PaymentStatusResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: PaymentStatusResponseData,
)
// -------------------------------------------------------

// PayByLink related ----------------------------------
data class PayByLinkRequestProduct(
    val Currency: String,
    val Name: String,
    val Quantity: Int,
    val Price: Float,
    val TotalPrice: String,
)

data class PayByLinkRequest(
    val ExpiryDate: String,
    val PBLLinkName: String,
    val Product: List<PayByLinkRequestProduct>,
)

data class PayByLinkResponseDataProduct(
    val Id: String,
    val Name: String,
    val Price: Float,
    val Currency: String,
    val Quantity: Int,
    val TotalPrice: String,
)

data class PayByLinkResponseData(
    val ProductID: String,
    val DASMID: String,
    val Product: List<PayByLinkResponseDataProduct>,
    val Amount: Float,
    val status: String, //"ACTIVE"
    val CreatedAt: String,
    val UpdatedAt: String,
    val ExpiryDate: String,
    val ReminderDate: String,
    val IsCustomerStatus: Boolean,
    val Currency: String,
    val ReturnUrl: JSONObject, //{}
    val PBLLinkName: String,
    val ID: String,
)

data class PayByLinkResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: PayByLinkResponseData,
)
// -------------------------------------------------------

// Product categories related ----------------------------------
data class CategoryListDataRecord(
    val CategoryName: String,
    val CategoryDesc: String,
    val CategoryStatus: String,
    val MerchantID: String,
    val CategoryID: String,
    val CreatedAt: String,
    val UpdatedAt: String?,
    val DeletedAt: String?,
    val DeletedBy: String?,
)

data class CategoryListData(
    val total_count: Int,
    val records: List<CategoryListDataRecord>,
)

data class CategoryListResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: CategoryListData,
)
// -------------------------------------------------------

// Product related ---------------------------------------
@Serializable
data class ProductListDataRecord(
    val CategoryID: String,
    val ProductName: String,
    val ProductDesc: String,
    val ProductStatus: Boolean,
    val ProductPrice: Float,
    val ProductID: String,
    val ProductImage: String?,
    val ProductCode: String,
    val ProductFoodType: String,
    val ProductSize: String,
    val CreatedAt: String?,
    val UpdatedAt: String?,
    val ProductStock: Int,
    val MerchantID: String,
    val Currency: String,
    val DeletedAt: String?,
    val DeletedBy: String?,
)

@Serializable
data class ProductRequest (
    val ProductName: String?,
    val ProductDesc: String?,
    val ProductPrice: Float?,
    val ProductFoodType: String?,
    val ProductSize: String?,
    val ProductCode: String?,
    val ProductStatus: Boolean?,
    val ProductStock: Long?,
    val Currency: String?,
    val MerchantID: String?,
    val CategoryID: String?
)

data class ProductListResponseData(
    val total_count: Int,
    val records: List<ProductListDataRecord>,
)

data class ProductListResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: ProductListResponseData,
)
// -------------------------------------------------------

//Data model for APIs call POST External Device Complete Registration and GET External Device Confirmation
@Serializable
data class CompleteDeviceRegistrationResponse(
    val status: Int,
    val message: String,
    val messageCode: String,//added this field based on error response
    val success: Boolean,
)

@Serializable
data class ExternalConfigurationResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: ExternalConfigData,
)

@Serializable
data class ExternalConfigData(
    val deviceInfo: DeviceInfo,
    val paymentMethod: List<DevicePaymentMethod>,
)

@Serializable
data class DeviceInfo(
    val DeviceID: String,
    val DeviceNumber: String,
    val DASMID: String,
    val DeviceMetadata: DeviceMetadata,
    val DeviceName: String,
    val DeviceType: String,
    val Status: String,
    val LastUsedAt: String,
    val CreatedAt: String,
    val UpdatedAt: String,
    val Location: String? = null,
)

@Serializable
data class DeviceMetadata(
    val os: String,
    val version: String,
    val manufacturer: String,
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
    val DASMID: String,
    val TransactionCCY: List<String>,
    val SettlementCCY: String,
    val schemes: DevicePaymentMethod_Schemes,
    val apms: DevicePaymentMethod_Apms,
    val Type: String,
    val Status: String,
)

@Serializable
data class CompleteDeviceRegistrationRequest(
    val UniqueCode: String,
    val DeviceNumber: String,
    val DeviceMetadata: DeviceMetadata,
    val DeviceType: String = "MOBILE",
)
// -------------------------------------------------------


// Insights related ---------------------------------------
data class InsightsResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: InsightsResponseData,
)

data class InsightsResponseData(
    val total: Int,
    val skip: Int,
    val take: Int,
    val records: List<InsightsResponseDataRecord>,
)

data class InsightsResponseDataRecord(
    val uuid: String,
    val status: String, //"NOTSUCCESSFUL"
    val TerminalID: String,
    val ID: String,
    val amount: Float,
    val CurrencyCode: String,
    val TransactionDate: String,
    val paymentMethod: String,
    val event: String,
    val TransactionType: String,
    val DASMID: String?,
    val AcquirerTransactionID: String?,
    val BatchID: String?,
    val BatchNo: String?,
    val SettleStatus: String?,
    val UpdatedAt: String?,
    val ProductType: String?,
    val IsVoided: Boolean?,
    val IsRefunded: Boolean?,
)
// -------------------------------------------------------

// Stats v2 related ---------------------------------------
data class StatsV2Request(
    val TimeZone: String,
    val Currency: String,
)

data class StatsV2Response(
    val dummy: String,
)
// -------------------------------------------------------


// Payment Details related ---------------------------------------
data class PaymentDetailsResponse(
    val statusCode: Int,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: PaymentDetailsResponseData,
)

data class PaymentDetailsResponseData(
    val TransactionRefID: String,
    val V2UUID: String?,
    val MerchantID: String,
    val Isrecurring: Boolean,
    val TransactionType: String,
    val Date: String,
    val UpdatedDate: String,
    val Amount: Float,
    val DASMID: String,
    val CurrencyCode: String,
    val Response: Int,
    val trackID: String,
    val Status: String,
    val AuthCode: String?,
    val CardHolder: String,
    val CVVResponse: String,
    val ExpiryDate: String,
    val Memo: String?,
    val AcquirerReferenceNumber: String,
    val Scheme: String,
    val TransactionID: String,
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
    val BIN: Int,
    val IssuingBank: String,
    val IssuingCountry: String,
    val MerchantRefNumber: String,
    val TransactionTimezone: String,
    val AcquirerID: String,
    val TerminalID: String,
    val browser_info: String?,
    val MerchantCategoryCode: String,
    val ProductType: String,
    val PrimaryAddress: PrimaryAddress?,
    val Merchant: String,
    val Referenceremark: String,
    val LegalNameInEnglish: String,
    val SecretKey: String,
    val TransactionLog: Any, // []
    val TokenizedTransactionHistory: Any, // [],
    val ProductDetails: Any, // [],
    val SubscriptionDetails: String?,
    val PaymentType: String,
    val AcquirerResponse: List<String?>,
    val transactionHistory: List<PaymentDetailsResponseData_TransactionHistory>,
)

data class PrimaryAddress(
    val Line1: String?,
    val Line2: String?,
    val Line3: String?,
    val Line4: String?,
    val Locality: String?,
    val Region: String?,
    val PostCode: String?,
    val Country: String?
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
data class PaymentDetailsResponseData_TransactionHistory(
    val uuid: String,
    val trackid: String?,
    val event: String?,
    val TransactionType: String,
    val amount: Float,
    val status: String,
    val ResponseCodeID: Int,
    val CreatedAt: String,
    val CurrencyCode: String,
    val Isrecurring: Boolean,
)

// -------------------------------------------------------
//Signature API Models
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
data class SettleBatchRequest(
    val batchId: String,
)


data class UploadImageResponse (
    val statusCode: Long,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: UploadImageData
)

data class UploadImageData (
    val signedUrl: String,
    val fileName: String
)


data class ProductResponse (
    val statusCode: Long,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: ProductListDataRecord
)

data class AppConfigResponse (
    val statusCode: Long,
    val message: String,
    val messageCode: String,
    val success: Boolean,
    val data: List<AppConfig>
)

data class AppConfig (
    val ID: Long,
    val AppENV: String,
    val BaseAPIURL: String,
    val RegistryLogin: String,
    val RegistryToken: String
)

data class SettlementListResponse (
    val statusCode: Long,
    val success: Boolean,
    val message: String,
    val data: SettlementData
)

data class SettlementData (
    val totalCount: Long,
    val take: Long,
    val skip: Long,
    val records: List<SettlementRecord>
)

data class SettlementRecord (
    val ID: Long,
    val uuid: String,
    val BatchID: String,
    val BatchNo: String,
    val SettleStatus: String,
    val CreatedAt: String,
    val UpdatedAt: String? = null,
    val SettledAt: String? = null,
    val Capture: Long,
    val CaptureAmount: Double,
    val Sale: Long,
    val SaleAmount: Double,
    val Refund: Long,
    val RefundAmount: Double,
    val Voided: Long,
    val VoidedAmount: Double
)

data class SettleBatchResponse (
    val status_code: Long,
    val BatchID: String,
    val SettleStatus: String,
    val SettlementRes: String
)

fun InsightsResponseDataRecord.toTransactionListDataRecord(): TransactionListDataRecord {
    return TransactionListDataRecord(
        uuid = this.uuid,
        V2UUID = this.uuid,

        TransactionType = this.TransactionType,
        amount = this.amount.toString(),
        CurrencyCode = this.CurrencyCode,
        status = this.status,

        PaymentType = this.paymentMethod,
        ProductType = this.ProductType ?: "",

        Date = this.TransactionDate,
        UpdatedDate = this.TransactionDate,

        TerminalId = this.TerminalID,
        TerminalName = this.TerminalID,

        TransactionID = this.ID.toIntOrNull() ?: 0,

        SettleStatus = this.SettleStatus,

        BatchID = this.BatchID,
        BatchNo = this.BatchNo,
        AcquirerTransactionID = this.AcquirerTransactionID,
        DASMID = this.DASMID ?: "",
        IsVoided = this.IsVoided,
        IsRefunded = this.IsRefunded,

        // ---- Fields not available → defaults ----
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
        SettledAt = "N/A",
        SubscriptionId = "",
        PBLLinkName = "N/A",
        GatewayResponse = "",
        ResponseCode = "",
        IntegrationType = "",
        has3DS = false,
    )
}

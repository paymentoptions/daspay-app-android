package com.paymentoptions.pos.services.apiService

import kotlinx.serialization.Serializable
import org.json.JSONObject

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
    val accessLevel: String,
    val signInAsMerchant: Boolean,
    val passwordExpiry: String,
)

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
    val TimeZone: String = "Indian/Mahe",
    val filter: List<TransactionListV2RequestFilter>,
)

data class TransactionListDataRecord(
    val uuid: String,
    val MerchantRefID: String,
    val LegalName: String,
    val LegalNameInEnglish: String,
    val DASMID: String,
    val trackID: String,
    val AcquirerMID: String,
    val TransactionType: String,
    val Scheme: String,
    val amount: String,
    val CurrencyCode: String,
    val status: String,
    val CardNumber: String,
    val AcquirerCode: String,
    val has3DS: Boolean,
    val AuthCode: String,
    val Isrecurring: Boolean,
    val Date: String,
    val V2UUID: String,
    val ProductType: String,
    val SubscriptionId: String,
    val UpdatedDate: String,
    val TerminalId: String,
    val TerminalName: String,
    val PBLLinkName: String,
    val IsWhitelisted: Boolean,
    val GatewayResponse: String,
    val ResponseCode: String,
    val TransactionID: Int,
    val IntegrationType: String,
    val PaymentType: String,
)

data class TransactionListData(
    val total_count: Int,
    val records: Array<TransactionListDataRecord>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionListData

        if (total_count != other.total_count) return false
        if (!records.contentEquals(other.records)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = total_count
        result = 31 * result + records.contentHashCode()
        return result
    }
}

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
    val refundAmount: Float,
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

data class RefundResponse(
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

data class PaymentStatusAmount(
    val currency: String,
    val value: Float,
)

data class PaymentStatusRequest(
    val tranId: String,
    val cvmPerformed: String = "NO_CVM",
    val tsi: String = "0000",
    val mcc: String = "",
    val merchantName: String = "2C2P",
    val tranStatus: String = "APPROVED",
    val tranType: String = "SALE",
    val atc: String = "01B4",
    val createdAt: String = "1719878285879",
    val trace: String = "491586",
    val acqMid: String = "065240116000002",
    val callbackUrl: String = "https://webhook.site/cdaa023f-fd59-4286-a241-1b120fbf1454",
    val entryMode: String = "NFC",
    val updatedAt: String = "1719878291684",
    val amount: PaymentStatusAmount,
    val batchNo: String = "101318",
    val appName: String = "5649534120435245444954",
    val linkedTranId: String = "33961491-58f2-42e5-902e-f955d68d2a01",
    val acqTid: String = "90004300",
    val merchantAddr: String = "",
    val rrn: String = "407020000174",
    val tc: String = "437484CAB4A9557A",
    val tvr: String = "0000000000",
    val accountMasked: String = "**** **** **** 2377",
    val sdkId: String = "515cd36f3f09bfc2",
    val paymentMethod: String = "VISA",
    val hostMessageFormat: String = "MS_ENABLER",
    val notifyId: Int = 1433,
    val aid: String = "A0000000031010",
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
    val ProductId: String,
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
//    val data: ExternalConfigData,
)
// -------------------------------------------------------
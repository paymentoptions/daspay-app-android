package com.paymentoptions.pos.services.apiService

import kotlinx.serialization.Serializable

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

//
//data class PayByLinkRequestProduct(
//    val Currency: String,
//    val Name: String,
//    val Quantity: Int,
//    val Price: Float,
//    val TotalPrice: String
//)
//
//data class PayByLinkRequest(
//    val ExpiryDate: String,
//    val PBLLinkName: String,
//    val Product: List<PayByLinkRequestProduct>,
//)
//
//data class PayByLinkResponse(
//    val status: Int,
//    val success: Boolean,
//    val message: Int,
//    val is_live: Boolean,
//    val transaction_type: String
//)

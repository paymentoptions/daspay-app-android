@file:Suppress("unused")
package com.paymentoptions.pos.services.apiService

import com.paymentoptions.pos.network.AccessLevel
import com.paymentoptions.pos.network.Address
import com.paymentoptions.pos.network.AppConfig
import com.paymentoptions.pos.network.AppConfigResponse
import com.paymentoptions.pos.network.AquirerResponse
import com.paymentoptions.pos.network.AquirerResponseAction
import com.paymentoptions.pos.network.AquirerResponseAmount
import com.paymentoptions.pos.network.BillingDetails
import com.paymentoptions.pos.network.Card
import com.paymentoptions.pos.network.CategoryListData
import com.paymentoptions.pos.network.CategoryListDataRecord
import com.paymentoptions.pos.network.CategoryListResponse
import com.paymentoptions.pos.network.CompleteDeviceRegistrationRequest
import com.paymentoptions.pos.network.CompleteDeviceRegistrationResponse
import com.paymentoptions.pos.network.DeviceInfo
import com.paymentoptions.pos.network.DeviceMetadata
import com.paymentoptions.pos.network.DevicePaymentMethod
import com.paymentoptions.pos.network.DevicePaymentMethod_Apms
import com.paymentoptions.pos.network.DevicePaymentMethod_Schemes
import com.paymentoptions.pos.network.ExternalConfigData
import com.paymentoptions.pos.network.ExternalConfigurationResponse
import com.paymentoptions.pos.network.GatewayResponse
import com.paymentoptions.pos.network.GetSignatureResponse
import com.paymentoptions.pos.network.InsightsResponse
import com.paymentoptions.pos.network.InsightsResponseData
import com.paymentoptions.pos.network.InsightsResponseDataRecord
import com.paymentoptions.pos.network.PayByLinkRequest
import com.paymentoptions.pos.network.PayByLinkRequestProduct
import com.paymentoptions.pos.network.PayByLinkResponse
import com.paymentoptions.pos.network.PayByLinkResponseData
import com.paymentoptions.pos.network.PayByLinkResponseDataProduct
import com.paymentoptions.pos.network.PaymentDetails
import com.paymentoptions.pos.network.PaymentDetailsResponse
import com.paymentoptions.pos.network.PaymentDetailsResponseData
import com.paymentoptions.pos.network.PaymentMethodRequest
import com.paymentoptions.pos.network.PaymentRequest
import com.paymentoptions.pos.network.PaymentResponse
import com.paymentoptions.pos.network.PaymentReturnUrl
import com.paymentoptions.pos.network.PaymentStatusRequest
import com.paymentoptions.pos.network.PrimaryAddress
import com.paymentoptions.pos.network.ProductImageRequest
import com.paymentoptions.pos.network.ProductListDataRecord
import com.paymentoptions.pos.network.ProductListResponse
import com.paymentoptions.pos.network.ProductListResponseData
import com.paymentoptions.pos.network.ProductRequest
import com.paymentoptions.pos.network.ProductResponse
import com.paymentoptions.pos.network.RefreshTokenRequest
import com.paymentoptions.pos.network.RefundPaymentDetailsCard
import com.paymentoptions.pos.network.RefundResponse
import com.paymentoptions.pos.network.Refund_MerchantDetails
import com.paymentoptions.pos.network.Refund_PaymentDetails
import com.paymentoptions.pos.network.Refund_PaymentDetails_AdditionalData
import com.paymentoptions.pos.network.Refund_PaymentDetails_AdditionalData_PaymentDataSource
import com.paymentoptions.pos.network.Refund_TransactionDetails
import com.paymentoptions.pos.network.RiskDetails
import com.paymentoptions.pos.network.SettleBatchRequest
import com.paymentoptions.pos.network.SettleBatchResponse
import com.paymentoptions.pos.network.SettlementData
import com.paymentoptions.pos.network.SettlementListResponse
import com.paymentoptions.pos.network.SettlementRecord
import com.paymentoptions.pos.network.SignInData
import com.paymentoptions.pos.network.SignInRequest
import com.paymentoptions.pos.network.SignInResponse
import com.paymentoptions.pos.network.SignOutRequest
import com.paymentoptions.pos.network.SignOutResponse
import com.paymentoptions.pos.network.SignatureData
import com.paymentoptions.pos.network.StatsV2Request
import com.paymentoptions.pos.network.StatsV2Response
import com.paymentoptions.pos.network.Token
import com.paymentoptions.pos.network.TransactionDetails
import com.paymentoptions.pos.network.TransactionListData
import com.paymentoptions.pos.network.TransactionListDataRecord
import com.paymentoptions.pos.network.TransactionListResponse
import com.paymentoptions.pos.network.TransactionListV2Request
import com.paymentoptions.pos.network.TransactionListV2RequestFilter
import com.paymentoptions.pos.network.TransactionRequest
import com.paymentoptions.pos.network.UploadImageData
import com.paymentoptions.pos.network.UploadImageResponse
import com.paymentoptions.pos.network.UploadSignatureResponse

/**
 * Type aliases so existing screens and files that import from this package continue to compile
 * without modification after the migration to the shared Ktor-based network layer.
 *
 * All real definitions live in com.paymentoptions.pos.network (shared module).
 */

// ── Auth ──────────────────────────────────────────────────────────────────────
typealias Token                        = Token
typealias SignInData                   = SignInData
typealias AccessLevel                  = AccessLevel
typealias SignInRequest                = SignInRequest
typealias SignInResponse               = SignInResponse
typealias SignOutRequest               = SignOutRequest
typealias SignOutResponse              = SignOutResponse
typealias RefreshTokenRequest          = RefreshTokenRequest

// ── Transaction list ──────────────────────────────────────────────────────────
typealias TransactionListV2RequestFilter = TransactionListV2RequestFilter
typealias TransactionListV2Request     = TransactionListV2Request
typealias TransactionListDataRecord    = TransactionListDataRecord
typealias TransactionListData          = TransactionListData
typealias TransactionListResponse      = TransactionListResponse

// ── Refund / Void ─────────────────────────────────────────────────────────────
typealias TransactionRequest           = TransactionRequest
typealias GatewayResponse              = GatewayResponse
typealias Refund_MerchantDetails       = Refund_MerchantDetails
typealias RefundPaymentDetailsCard     = RefundPaymentDetailsCard
typealias Refund_PaymentDetails_AdditionalData_PaymentDataSource = Refund_PaymentDetails_AdditionalData_PaymentDataSource
typealias Refund_PaymentDetails_AdditionalData = Refund_PaymentDetails_AdditionalData
typealias Refund_PaymentDetails        = Refund_PaymentDetails
typealias Refund_TransactionDetails    = Refund_TransactionDetails
typealias RefundResponse               = RefundResponse

// ── Payment ───────────────────────────────────────────────────────────────────
typealias PaymentReturnUrl             = PaymentReturnUrl
typealias Address                      = Address
/** Maps to [PaymentMethodRequest] */
typealias PaymentMethod                = PaymentMethodRequest
typealias PaymentRequest               = PaymentRequest
typealias Card                         = Card
typealias PaymentDetails               = PaymentDetails
typealias TransactionDetails           = TransactionDetails
typealias BillingDetails               = BillingDetails
typealias RiskDetails                  = RiskDetails
typealias PaymentResponse              = PaymentResponse

// ── PayByLink ─────────────────────────────────────────────────────────────────
typealias PayByLinkRequestProduct      = PayByLinkRequestProduct
typealias PayByLinkRequest             = PayByLinkRequest
typealias PayByLinkResponseDataProduct = PayByLinkResponseDataProduct
typealias PayByLinkResponseData        = PayByLinkResponseData
typealias PayByLinkResponse            = PayByLinkResponse

// ── Categories ────────────────────────────────────────────────────────────────
typealias CategoryListDataRecord       = CategoryListDataRecord
typealias CategoryListData             = CategoryListData
typealias CategoryListResponse         = CategoryListResponse

// ── Products ──────────────────────────────────────────────────────────────────
typealias ProductListDataRecord        = ProductListDataRecord
typealias ProductRequest               = ProductRequest
typealias ProductListResponseData      = ProductListResponseData
typealias ProductListResponse          = ProductListResponse
typealias ProductResponse              = ProductResponse
typealias ProductImageRequest          = ProductImageRequest
typealias UploadImageResponse          = UploadImageResponse
typealias UploadImageData              = UploadImageData

// ── Device registration ────────────────────────────────────────────────────────
typealias CompleteDeviceRegistrationResponse = CompleteDeviceRegistrationResponse
typealias CompleteDeviceRegistrationRequest  = CompleteDeviceRegistrationRequest
typealias ExternalConfigurationResponse      = ExternalConfigurationResponse
typealias ExternalConfigData                 = ExternalConfigData
typealias DeviceInfo                         = DeviceInfo
typealias DeviceMetadata                     = DeviceMetadata
typealias DevicePaymentMethod_Schemes        = DevicePaymentMethod_Schemes
typealias DevicePaymentMethod_Apms           = DevicePaymentMethod_Apms
typealias DevicePaymentMethod                = DevicePaymentMethod

// ── Insights ──────────────────────────────────────────────────────────────────
typealias InsightsResponse             = InsightsResponse
typealias InsightsResponseData         = InsightsResponseData
typealias InsightsResponseDataRecord   = InsightsResponseDataRecord

// ── Stats v2 ──────────────────────────────────────────────────────────────────
typealias StatsV2Request               = StatsV2Request
typealias StatsV2Response              = StatsV2Response

// ── Payment details ───────────────────────────────────────────────────────────
typealias PaymentDetailsResponse       = PaymentDetailsResponse
typealias PaymentDetailsResponseData   = PaymentDetailsResponseData
typealias PrimaryAddress               = PrimaryAddress
typealias AquirerResponse              = AquirerResponse
typealias AquirerResponseAmount        = AquirerResponseAmount
typealias AquirerResponseAction        = AquirerResponseAction

// ── Signature upload ──────────────────────────────────────────────────────────
typealias UploadSignatureResponse      = UploadSignatureResponse

typealias GetSignatureResponse         = GetSignatureResponse
typealias SignatureData = SignatureData
typealias PaymentStatusRequest = PaymentStatusRequest


// ── Settlement ────────────────────────────────────────────────────────────────
typealias SettleBatchRequest           = SettleBatchRequest
typealias SettleBatchResponse          = SettleBatchResponse
typealias SettlementListResponse       = SettlementListResponse
typealias SettlementData               = SettlementData
typealias SettlementRecord             = SettlementRecord

// ── App config ────────────────────────────────────────────────────────────────
typealias AppConfigResponse            = AppConfigResponse
typealias AppConfig                    = AppConfig



package com.paymentoptions.pos.services.apiService

import com.google.gson.GsonBuilder
import com.paymentoptions.pos.utils.retrofitTimeout
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import java.util.concurrent.TimeUnit

const val baseUrl: String = "https://api-dev.paymentoptions.com/api/v1/"

interface ApiService {
    @POST("auth/signIn/")
    suspend fun signIn(
        @HeaderMap headers: Map<String, String>,
        @Body request: SignInRequest,
    ): SignInResponse

    @POST("auth/signOut/")
    suspend fun signOut(
        @HeaderMap headers: Map<String, String>,
        @Body request: SignOutRequest,
    ): SignOutResponse

    @POST("auth/refreshToken/")
    suspend fun refreshToken(
        @HeaderMap headers: Map<String, String>,
        @Body request: RefreshTokenRequest,
    ): SignInResponse

    @POST("entities/merchant/devices/complete-registration")
    suspend fun completeDeviceRegistration(
        @HeaderMap headers: Map<String, String>,
        @Body request: CompleteDeviceRegistrationRequest,
    ): CompleteDeviceRegistrationResponse

    @GET("entities/merchant/devices/external/configuration")
    suspend fun getDeviceConfiguration(
        @HeaderMap headers: Map<String, String>,
        @Query("DeviceNumber") deviceNumber: String,
        @Query("UniqueCode") uniqueCode: String,
    ): ExternalConfigurationResponse

    @GET("transactions/list")
    suspend fun transactionList(
        @HeaderMap headers: Map<String, String>,
        @Query("take") take: Int,
        @Query("skip") skip: Int,
    ): TransactionListResponse

    @POST("transactions/listv2")
    suspend fun transactionListV2(
        @HeaderMap headers: Map<String, String>,
        @Body request: TransactionListV2Request,
    ): TransactionListResponse

    @POST("paybylink/add/DASMID/{dasmid}")
    suspend fun payByLink(
        @HeaderMap headers: Map<String, String>,
        @Path("dasmid") dasmid: String,
        @Body request: PayByLinkRequest,
    ): PayByLinkResponse

    @POST("transactions/refund")
    suspend fun refund(
        @HeaderMap headers: Map<String, String>,
        @Body request: RefundRequest,
    ): RefundResponse

    @POST("server-to-server-interface/daspay/payment")
    suspend fun payment(
        @HeaderMap headers: Map<String, String>,
        @Body request: PaymentRequest,
    ): PaymentResponse

    @POST("notification/payments/webhook/notify/minesec")
    suspend fun paymentStatus(
        @HeaderMap headers: Map<String, String>,
        @Body request: PaymentStatusRequest,
    ): String

    @GET("entities/merchant/catalog/all-categories/{merchantId}")
    suspend fun categoryList(
        @HeaderMap headers: Map<String, String>,
        @Path("merchantId") merchantId: String,
    ): CategoryListResponse

    @GET("entities/merchant/catalog/all-products/{categoryId}")
    suspend fun productList(
        @HeaderMap headers: Map<String, String>,
        @Path("categoryId") categoryId: String,
    ): ProductListResponse

    @GET("daspay/transactions/list")
    suspend fun insights(
        @HeaderMap headers: Map<String, String>,
        @Query("deviceNumber") deviceNumber: String = "12345678kg1",
        @Query("uniqueCode") uniqueCode: String = "213fsdHJ51MOBILEKG1",
//        @Query("TimeZone") timeZone: String = "undefined",
        @Query("startDate") startDate: String = "undefined",
        @Query("endDate") endDate: String = "undefined",
        @Query("take") take: Int,
    ): InsightsResponse

    @POST("transactions/stats")
    suspend fun statsV2(
        @HeaderMap headers: Map<String, String>,
        @Body request: StatsV2Request,
    ): StatsV2Response

    @GET("transactions/{paymentId}")
    suspend fun paymentDetails(
        @HeaderMap headers: Map<String, String>,
        @Path("paymentId") paymentId: String,
    ): PaymentDetailsResponse

    @FormUrlEncoded
    @POST("entities/minesec/generate-image")
    suspend fun uploadSignature(
        @HeaderMap headers: Map<String, String>,
        @Field("signature") signature: String,
        @Field("TransactionID") TransactionID: String,
    ): UploadSignatureResponse
}

var gson = GsonBuilder()
    .setLenient()
    .create()

val logging = HttpLoggingInterceptor().apply {
    setLevel(HttpLoggingInterceptor.Level.BODY)
}

var okHttpClient = OkHttpClient.Builder()
    .addInterceptor(logging)
    .connectTimeout(retrofitTimeout, TimeUnit.SECONDS) // Time to establish the connection
    .readTimeout(retrofitTimeout, TimeUnit.SECONDS) // Time to wait for the server to send data
    .writeTimeout(retrofitTimeout, TimeUnit.SECONDS) // Time to send data to the server
    .build()

object RetrofitClient {
    val api: ApiService by lazy {
        Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
            .create(ApiService::class.java)
    }
}



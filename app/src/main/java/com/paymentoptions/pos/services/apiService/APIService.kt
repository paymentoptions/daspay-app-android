package com.paymentoptions.pos.services.apiService

import com.paymentoptions.pos.utils.retrofitTimeout
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


const val baseUrl: String = "https://api-dev.paymentoptions.com/api/v1/api/v1/"

interface ApiService {
    //Auth
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
}

var okHttpClient = OkHttpClient.Builder()
    .connectTimeout(retrofitTimeout, TimeUnit.SECONDS) // Time to establish the connection
    .readTimeout(retrofitTimeout, TimeUnit.SECONDS) // Time to wait for the server to send data
    .writeTimeout(retrofitTimeout, TimeUnit.SECONDS) // Time to send data to the server
    .build()

object RetrofitClient {
    val api: ApiService by lazy {
        Retrofit.Builder().baseUrl(baseUrl).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiService::class.java)
    }
}



package com.paymentoptions.pos.services.apiService

import com.google.gson.GsonBuilder
import com.paymentoptions.pos.logger.AppLogger
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
import retrofit2.http.PUT
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

    @POST("transactions/daspay-list")
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


    @POST("entities/merchant/catalog/products")
    suspend fun addProduct(
        @HeaderMap headers: Map<String, String>,
        @Body request: ProductRequest,
    ): ProductResponse

    @PUT("entities/merchant/catalog/products/{productId}")
    suspend fun editProduct(
        @HeaderMap headers: Map<String, String>,
        @Body request: ProductRequest,
        @Path("productId") productId: String,
    ): ProductResponse

    @POST("entities/merchant/catalog/upload-products-images")
    suspend fun uploadProductImage(
        @HeaderMap headers: Map<String, String>,
        @Body request: ProductImageRequest,
    ): UploadImageResponse

}

var gson = GsonBuilder()
    .setLenient()
    .create()

val logging = HttpLoggingInterceptor { message -> AppLogger.info(message) }
.apply { level = HttpLoggingInterceptor.Level.BODY }

fun provideOkHttpClient(context: android.content.Context): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(logging)
        .authenticator(TokenAuthenticator(context))
        .connectTimeout(retrofitTimeout, TimeUnit.SECONDS)
        .readTimeout(retrofitTimeout, TimeUnit.SECONDS)
        .writeTimeout(retrofitTimeout, TimeUnit.SECONDS)
        .build()
}

object RetrofitClient {
    @Volatile
    private var apiService: ApiService? = null

    fun getApi(context: android.content.Context): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(provideOkHttpClient(context.applicationContext))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
                .also { apiService = it }
        }
    }
}



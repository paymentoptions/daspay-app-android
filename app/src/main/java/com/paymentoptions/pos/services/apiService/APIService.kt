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
        @Body post: SignInRequest,
    ): SignInResponse

    @POST("auth/signOut/")
    suspend fun signOut(
        @HeaderMap headers: Map<String, String>,
        @Body post: SignOutRequest,
    ): SignOutResponse

    @POST("auth/refreshToken/")
    suspend fun refreshToken(
        @HeaderMap headers: Map<String, String>,
        @Body post: RefreshTokenRequest,
    ): SignInResponse

    @GET("transactions/list")
    suspend fun transactionList(
        @HeaderMap headers: Map<String, String>,
        @Query("take") take: Int,
        @Query("skip") skip: Int,
    ): TransactionListResponse


//    @POST("paybylink/add/DASMID/JP00002347")
//    suspend fun payByLink(
//        @HeaderMap headers: Map<String, String>,
//        @Body post: PayByLinkRequest,
//    ): PayByLinkResponse

    @POST("transactions/refund")
    suspend fun refund(
        @HeaderMap headers: Map<String, String>,
        @Body post: RefundRequest,
    ): RefundResponse

    @POST("server-to-server-interface/daspay/payment")
    suspend fun payment(
        @HeaderMap headers: Map<String, String>,
        @Body post: PaymentRequest,
    ): PaymentResponse

    @GET("entities/merchant/catalog/all-categories/{dasmid}")
    suspend fun categoryList(
        @HeaderMap headers: Map<String, String>,
        @Path("dasmid") dasmid: String,
    ): CategoryListResponse

    @GET("entities/merchant/catalog/all-categories/{categoryId}")
    suspend fun productList(
        @HeaderMap headers: Map<String, String>,
        @Path("categoryId") categoryId: String = "8c5c19ef-91ef-4ad4-bbf3-118411e90065",
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



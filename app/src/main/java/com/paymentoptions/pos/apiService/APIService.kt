package com.paymentoptions.pos.apiService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query

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
    suspend fun transactionsList(
        @HeaderMap headers: Map<String, String>,
        @Query("take") take: Int,
        @Query("skip") skip: Int,
    ): TransactionListResponse

    @POST("server-to-server-interface/refund")
    suspend fun refund(
        @HeaderMap headers: Map<String, String>,
        @Body post: RefundRequest,
    ): RefundResponse
}

object RetrofitClient {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}



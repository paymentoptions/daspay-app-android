package com.paymentoptions.pos.services.apiService

import com.paymentoptions.pos.services.analytics.AppAnalytics
import okhttp3.Interceptor
import okhttp3.Response

class ApiErrorTrackingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            val response = chain.proceed(request)
            if (!response.isSuccessful) {
                val errorBody = response.peekBody(2048).string()
                AppAnalytics.apiError(
                    endpoint = request.url.encodedPath,
                    method = request.method,
                    code = response.code,
                    message = errorBody.ifBlank { response.message }
                )
            }
            response
        } catch (e: Exception) {
            AppAnalytics.apiError(
                endpoint = request.url.encodedPath,
                method = request.method,
                code = null,
                message = e.message
            )
            throw e
        }
    }
}


package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.services.apiService.ProductRequest
import com.paymentoptions.pos.services.apiService.ProductResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import java.io.File

/**
 * Add Product to the server.
 */
suspend fun addProduct(context: Context, request: ProductRequest, selectedFile : File?): ProductResponse? {
    try {
        println("addProduct: $request and selectedfile: $selectedFile")
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(idToken)

        val addProductResponse =
            RetrofitClient.getApi(context).addProduct(headers = requestHeaders, request = request)
        if(selectedFile != null && (addProductResponse.statusCode == 200L || addProductResponse.statusCode == 201L)){
            uploadMediaToProduct(context, requestHeaders, addProductResponse.data.ProductID, selectedFile)
        }
        return addProductResponse
    } catch (e: Exception) {
        println("addProductError: $e")
        throw e
    }
}
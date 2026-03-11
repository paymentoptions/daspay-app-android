package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import com.paymentoptions.pos.logger.AppLogger
import com.paymentoptions.pos.services.apiService.ProductRequest
import com.paymentoptions.pos.services.apiService.ProductResponse
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.TokenRepository
import com.paymentoptions.pos.services.apiService.generateRequestHeader
import com.paymentoptions.pos.ui.composables.screens._flow.foodOrderFlow.FoodItem
import java.io.File

/**
 * Edit Product API call.
 */
suspend fun editProduct(context: Context, request: ProductRequest,foodItem: FoodItem,
                        selectedFile: File?): ProductResponse? {
    try {
        val tokenRepository = TokenRepository.getInstance(context)
        val authDetails = tokenRepository.refreshTokenIfNeeded() ?: return null

        val idToken = authDetails.data.token.idToken
        val requestHeaders = generateRequestHeader(idToken)

        val editProductResponse =
            RetrofitClient.getApi(context).editProduct(headers = requestHeaders,
                request = request,
                productId = foodItem.item.ProductID)

        if(selectedFile != null && (editProductResponse.statusCode == 200L || editProductResponse.statusCode == 201L)){
            uploadMediaToProduct(context, requestHeaders, editProductResponse.data.ProductID, selectedFile)
        }
        return editProductResponse
    } catch (e: Exception) {
        AppLogger.error("editProduct Error: $e")
        throw e
    }
}
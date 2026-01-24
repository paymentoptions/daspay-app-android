package com.paymentoptions.pos.services.apiService.endpoints

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.paymentoptions.pos.device.SharedPreferences
import com.paymentoptions.pos.device.SharedPreferences.Companion.saveFcmToken
import com.paymentoptions.pos.services.apiService.RetrofitClient
import com.paymentoptions.pos.services.apiService.SignInRequest
import com.paymentoptions.pos.services.apiService.SignInResponse
import com.paymentoptions.pos.services.apiService.generateRequestHeader

suspend fun autoSignIn(context: Context): SignInResponse? {
    try {
        val authDetails = SharedPreferences.getSavedCredentials(context)
        val username = authDetails.first
        val password = authDetails.second

        val requestHeaders = generateRequestHeader()
        val signInRequest = SignInRequest(username!!, password!!)
        val signInResponse = RetrofitClient.getApi(context).signIn(requestHeaders, signInRequest)

        println("signInResponse: $signInResponse")


        signInResponse.let {
            if (signInResponse.success) {

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        saveFcmToken(context, token)
                           println("mainActivity token --> $token")
                    } else {
                        println("mainActivity token fetching failed ${task.exception}")
                    }
                }

                SharedPreferences.saveAuthDetails(context, signInResponse)
                //TokenRepository.getInstance(context).scheduleTokenRefresh(signInResponse.data.exp)
            }
        }
        return  signInResponse

    } catch (e: Exception) {
        Log.e("DEBUG_TOKEN", "Step 1 FAILED: autoSignIn.", e)
        throw e
    }
}
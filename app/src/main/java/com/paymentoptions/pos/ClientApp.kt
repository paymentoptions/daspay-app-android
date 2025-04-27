package com.paymentoptions.pos

import android.app.Application
import android.util.Log
import com.theminesec.sdk.headless.HeadlessSetup
import com.theminesec.sdk.headless.model.WrappedResult
import com.theminesec.sdk.headless.model.setup.SdkInitResp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

//import com.theminesec.minehades.config.MhdConfig

class ClientApp : Application() {
    private val appScope = CoroutineScope(Dispatchers.Main)
    private val _sdkInitStatus = MutableSharedFlow<WrappedResult<SdkInitResp>>(replay = 1)
    val sdkInitStatus: SharedFlow<WrappedResult<SdkInitResp>> = _sdkInitStatus

    override fun onCreate() {
        super.onCreate()

        appScope.launch {
            val clientAppInitRes =
                HeadlessSetup.initSoftPos(this@ClientApp, "payment-options.license")
            Log.d("ClientApp ->", "Application init: $clientAppInitRes")
            _sdkInitStatus.emit(clientAppInitRes)
        }
    }
}
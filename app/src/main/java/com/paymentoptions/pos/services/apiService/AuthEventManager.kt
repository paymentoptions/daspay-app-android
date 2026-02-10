package com.paymentoptions.pos.services.apiService

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Manages authentication events that need to be handled globally,
 * such as token expiration requiring re-authentication.
 */
object AuthEventManager {

    sealed class AuthEvent {
        /**
         * Emitted when refresh token is expired and user needs to re-authenticate
         * via fingerprint/biometric auto sign-in
         */
        object RequireReAuthentication : AuthEvent()

        /**
         * Emitted when auto sign-in fails and manual sign-in is required
         */
        object RequireManualSignIn : AuthEvent()
    }

    private val _authEvents = MutableSharedFlow<AuthEvent>(replay = 0, extraBufferCapacity = 1)
    val authEvents: SharedFlow<AuthEvent> = _authEvents.asSharedFlow()

    /**
     * Call this when refresh token expires (ERR_AUTH_0003)
     */
    fun onRefreshTokenExpired() {
        println("AuthEventManager: Refresh token expired, requiring re-authentication")
        _authEvents.tryEmit(AuthEvent.RequireReAuthentication)
    }

    /**
     * Call this when auto sign-in fails
     */
    fun onAutoSignInFailed() {
        println("AuthEventManager: Auto sign-in failed, requiring manual sign-in")
        _authEvents.tryEmit(AuthEvent.RequireManualSignIn)
    }
}


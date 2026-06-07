package com.paymentoptions.pos.auth

import com.paymentoptions.pos.platformLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Clock

/**
 * App-wide authentication event bus.
 *
 * Emit [AuthEvent.RequireReAuthentication] to send the user to the fingerprint screen.
 * Emit [AuthEvent.RequireManualSignIn] to send the user to the sign-in screen.
 *
 * The [Navigator] collects these events and reacts accordingly.
 */
object AuthEventManager {

    sealed class AuthEvent {
        /** Stored credentials exist – prompt for biometric/PIN. */
        object RequireReAuthentication : AuthEvent()
        /** No valid credentials; user must type their password again. */
        object RequireManualSignIn : AuthEvent()
    }

    private val _authEvents = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val authEvents: SharedFlow<AuthEvent> = _authEvents.asSharedFlow()

    private var lastEventTime = 0L
    private const val DEBOUNCE_MS = 2000L

    fun requireReAuthentication() {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastEventTime > DEBOUNCE_MS) {
            lastEventTime = now
            platformLog("AuthEventManager", "Emitting RequireReAuthentication event")
            _authEvents.tryEmit(AuthEvent.RequireReAuthentication)
        }
    }

    fun requireManualSignIn() {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastEventTime > DEBOUNCE_MS) {
            lastEventTime = now
            platformLog("AuthEventManager", "Emitting RequireManualSignIn event")
            _authEvents.tryEmit(AuthEvent.RequireManualSignIn)
        }
    }
}

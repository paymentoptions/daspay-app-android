package com.paymentoptions.pos.services.analytics

import com.datadog.android.rum.GlobalRumMonitor
import com.datadog.android.rum.RumActionType
import com.datadog.android.rum.RumErrorSource
import com.paymentoptions.pos.BuildConfig
import com.paymentoptions.pos.logger.AppLogger

object AnalyticsHelper {

    fun track(event: AnalyticsEvent) {
        if (!GlobalRumMonitor.isRegistered()) {
            AppLogger.warn("Analytics event dropped because Datadog RUM is not initialized: ${event.name}")
            return
        }

        val attributes = sanitizeAttributes(
            mapOf(
                "event_name" to event.name,
                "build_type" to BuildConfig.BUILD_TYPE,
                "flavor" to BuildConfig.FLAVOR,
                "environment" to BuildConfig.ENVIRONMENT
            ) + event.attributes
        )

        GlobalRumMonitor.get().addAction(RumActionType.CUSTOM, event.name, attributes)
        AppLogger.debug("Analytics event sent: ${event.name}, attributes=$attributes")
    }

    fun trackError(
        message: String,
        throwable: Throwable? = null,
        attributes: Map<String, Any?> = emptyMap()
    ) {
        if (!GlobalRumMonitor.isRegistered()) {
            AppLogger.warn("Analytics error dropped because Datadog RUM is not initialized: $message")
            return
        }

        val sanitizedAttributes = sanitizeAttributes(
            mapOf(
                "build_type" to BuildConfig.BUILD_TYPE,
                "flavor" to BuildConfig.FLAVOR,
                "environment" to BuildConfig.ENVIRONMENT
            ) + attributes
        )

        GlobalRumMonitor.get().addError(
            message,
            RumErrorSource.CUSTOM,
            throwable ?: IllegalStateException(message),
            sanitizedAttributes
        )
        AppLogger.error("Analytics error sent: $message")
    }

    private fun sanitizeAttributes(attributes: Map<String, Any?>): Map<String, Any> {
        return buildMap {
            attributes.forEach { (key, value) ->
                when (value) {
                    null -> Unit
                    is String, is Number, is Boolean -> put(key, value)
                    else -> put(key, value.toString())
                }
            }
        }
    }
}


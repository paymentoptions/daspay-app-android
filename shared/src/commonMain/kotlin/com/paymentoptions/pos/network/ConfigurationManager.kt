package com.paymentoptions.pos.network

import com.paymentoptions.pos.urlEncode
import com.paymentoptions.pos.storage.AppStorage

/**
 * Manages the active API base URL.
 *
 * Priority: AppStorage override → build-time default.
 */
object ConfigurationManager {

    /** Injected at app startup from BuildConfig (Android) / plist (iOS). */
    var buildTimeBaseUrl: String = "https://api-dev.paymentoptions.com/api/v1/"

    val activeBaseUrl: String
        get() = AppStorage.baseUrl?.takeIf { it.isNotBlank() }
            ?: AppStorage.configBaseUrl?.takeIf { it.isNotBlank() }
            ?: buildTimeBaseUrl

    fun url(path: String): String {
        val base = activeBaseUrl.trimEnd('/')
        val segment = path.trimStart('/')
        return "$base/$segment"
    }

    /**
     * Resolves a templated path like `entities/{merchantId}` with path params.
     */
    fun url(path: String, vararg pathParams: Pair<String, String>): String {
        val resolvedPath = pathParams.fold(path) { acc, (key, value) ->
            acc.replace("{$key}", urlEncode(value))
        }
        return url(resolvedPath)
    }
}

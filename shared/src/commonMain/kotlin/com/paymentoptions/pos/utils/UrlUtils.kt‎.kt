package com.paymentoptions.pos.utils

expect class UrlOpener {
    fun open(url: String?)
}

internal fun normalizeUrl(url: String?): String? {
    val trimmed = url?.trim()
    if (trimmed.isNullOrBlank()) return null
    val lower = trimmed.lowercase()
    return if (lower.startsWith("http://") || lower.startsWith("https://")) trimmed else "https://$trimmed"
}
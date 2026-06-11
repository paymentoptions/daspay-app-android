package com.paymentoptions.pos.logger

import platform.Foundation.NSLog

actual object AppLogger {
    actual fun debug(vararg objects: Any) {
        NSLog("DEBUG: ${objects.joinToString(" ")}")
    }

    actual fun info(vararg objects: Any) {
        NSLog("INFO: ${objects.joinToString(" ")}")
    }

    actual fun warn(vararg objects: Any) {
        NSLog("WARN: ${objects.joinToString(" ")}")
    }

    actual fun error(vararg objects: Any) {
        NSLog("ERROR: ${objects.joinToString(" ")}")
    }

    actual fun d(tag: String, vararg arguments: String) {
        NSLog("DEBUG [$tag]: ${arguments.joinToString(" ")}")
    }

    actual fun i(tag: String, vararg arguments: String) {
        NSLog("INFO [$tag]: ${arguments.joinToString(" ")}")
    }

    actual fun e(tag: String, vararg arguments: String) {
        NSLog("ERROR [$tag]: ${arguments.joinToString(" ")}")
    }
}

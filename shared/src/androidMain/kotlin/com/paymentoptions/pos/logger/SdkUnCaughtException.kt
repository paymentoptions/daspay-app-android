package com.paymentoptions.pos.logger

import android.os.Process
import android.util.Log
import com.paymentoptions.pos.platformLogError
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Uncaught Exception handler invoked when a Thread abruptly terminates due to an uncaught
 * exception.
 */
class SdkUnCaughtException(
    private val uncaughtExceptionHandler: Thread.UncaughtExceptionHandler?,
    private val onThrowError: (Throwable) -> Unit,
) : Thread.UncaughtExceptionHandler {
    /** Exception will write all stack trace to string builder. */
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // avoid loops if we fault
        thread.uncaughtExceptionHandler = null
        Thread.setDefaultUncaughtExceptionHandler(null)
        onThrowError(throwable)
        val result = StringWriter()
        val printWriter = PrintWriter(result)
        throwable.printStackTrace(printWriter)
        printWriter.close()
        platformLogError("Uncaught exception in $thread", result.toString())
        if (uncaughtExceptionHandler != null) {
            uncaughtExceptionHandler.uncaughtException(thread, throwable)
        } else {
            try {
                val pid = Process.myPid()
                Process.killProcess(pid)
            } catch (t2: Throwable) {
                Log.e("SdkUnCaughtException", t2.localizedMessage as String)
            }
        }
    }
}

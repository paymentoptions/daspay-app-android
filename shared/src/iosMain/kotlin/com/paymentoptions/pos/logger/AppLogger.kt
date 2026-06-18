package com.paymentoptions.pos.logger

import platform.Foundation.*
import platform.objc.objc_sync_enter
import platform.objc.objc_sync_exit

actual object AppLogger {
    private val lock = Any()
    private const val MAX_LOG_LENGTH = 1000
    private const val LOG_FILE_NAME = "daspay_log.txt"

    private val logFilePath: String? by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.firstOrNull() as? String ?: return@lazy null
        "$cacheDirectory/$LOG_FILE_NAME"
    }

    private fun synchronized(lock: Any, block: () -> Unit) {
        objc_sync_enter(lock)
        try {
            block()
        } finally {
            objc_sync_exit(lock)
        }
    }

    private fun writeToFile(text: String) {
        val path = logFilePath ?: return
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(path)) {
            fileManager.createFileAtPath(path, contents = null, attributes = null)
        }

        val fileHandle = NSFileHandle.fileHandleForWritingAtPath(path) ?: return
        fileHandle.seekToEndOfFile()
        val logLine = "$text\n"
        val data = (logLine as? NSString)?.dataUsingEncoding(NSUTF8StringEncoding)
        if (data != null) {
            fileHandle.writeData(data)
        }
        fileHandle.closeFile()
    }

    private fun logMessage(level: String, tag: String?, objects: Array<out Any>) {
        val message = objects.joinToString(" ")
        val header = if (tag != null) "$level [$tag]: " else "$level: "
        
        synchronized(lock) {
            val lines = message.split('\n')
            for ((lineIndex, line) in lines.withIndex()) {
                val linePrefix = if (lineIndex == 0) header else "  "
                
                if (line.isEmpty() && lineIndex == 0) {
                    val fullLine = linePrefix
                    NSLog(fullLine)
                    writeToFile(fullLine)
                    continue
                }
                
                var start = 0
                while (start < line.length) {
                    val end = minOf(start + MAX_LOG_LENGTH, line.length)
                    val chunk = line.substring(start, end)
                    val chunkPrefix = if (start == 0) linePrefix else "..."
                    val fullLine = "$chunkPrefix$chunk"
                    NSLog(fullLine)
                    writeToFile(fullLine)
                    start = end
                }
            }
        }
    }

    actual fun debug(vararg objects: Any) = logMessage("DEBUG", null, objects)
    actual fun info(vararg objects: Any) = logMessage("INFO", null, objects)
    actual fun warn(vararg objects: Any) = logMessage("WARN", null, objects)
    actual fun error(vararg objects: Any) = logMessage("ERROR", null, objects)

    actual fun d(tag: String, vararg arguments: String) = 
        logMessage("DEBUG", tag, arguments.map { it as Any }.toTypedArray())
    
    actual fun i(tag: String, vararg arguments: String) = 
        logMessage("INFO", tag, arguments.map { it as Any }.toTypedArray())
    
    actual fun e(tag: String, vararg arguments: String) = 
        logMessage("ERROR", tag, arguments.map { it as Any }.toTypedArray())

    fun getLogFileUrl(): NSURL? {
        return logFilePath?.let { NSURL.fileURLWithPath(it) }
    }
}

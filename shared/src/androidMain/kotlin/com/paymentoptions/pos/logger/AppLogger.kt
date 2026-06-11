package com.paymentoptions.pos.logger

import android.os.Process
import android.util.Log
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException

actual object AppLogger {
    private const val TAG = "DASPAY"
    var IS_DEBUG_ENABLED = true
    var IS_INFO_ENABLED = true
    var IS_WARN_ENABLED = true
    var IS_ERROR_ENABLED = true
    var appVersion: String? = null

    private val logger: Logger = LogManager.getLogger("DASPAY_LOGGER")
    private const val FILE_NAME = "daspay_log"
    private const val CHECK_LOCK = true
    private val headerSb = StringBuilder()
    private val lock = Any()
    private val msgSB = StringBuilder()

    fun init(
        context: android.content.Context,
        config: Config,
        appVersion: String,
        onThrowError: (Throwable) -> Unit,
    ) {
        this.appVersion = appVersion
        setLogLevel(config.logConfig!!.logLevel)
        configureLogger(context, config.logConfig.logFolderPath, config.logConfig.logLevel)
        Thread.setDefaultUncaughtExceptionHandler(
            SdkUnCaughtException(Thread.getDefaultUncaughtExceptionHandler(), onThrowError),
        )
    }

    private fun setLogLevel(logLevel: Level) {
        when (logLevel) {
            Level.ERROR -> {
                IS_DEBUG_ENABLED = false
                IS_INFO_ENABLED = false
                IS_WARN_ENABLED = false
            }

            Level.WARN -> {
                IS_DEBUG_ENABLED = false
                IS_INFO_ENABLED = false
                IS_WARN_ENABLED = true
            }

            Level.INFO -> {
                IS_DEBUG_ENABLED = false
                IS_INFO_ENABLED = true
                IS_WARN_ENABLED = true
            }

            Level.OFF -> {
                IS_DEBUG_ENABLED = false
                IS_INFO_ENABLED = false
                IS_WARN_ENABLED = false
            }

            else -> {
                IS_DEBUG_ENABLED = true
                IS_INFO_ENABLED = true
                IS_WARN_ENABLED = true
            }
        }
    }

    private fun configureLogger(context: android.content.Context, logFolderPath: String?, logLevel: Level) {
        val rootPath = logFolderPath ?: context.cacheDir.absolutePath
        debug("SDK: Log Root Path $rootPath")
        val cfg = LogConfigurator(rootPath + File.separator + "Log" + File.separator + FILE_NAME)
        try {
            cfg.configureFileAppender(logger, logLevel)
            debug("Configured Log4j Successfully, file = ${cfg.fileName} ")
        } catch (e: IOException) {
            error("Error configuring log4J at ${cfg.fileName}: ", e)
        }
    }

    actual fun debug(vararg objects: Any) {
        if (IS_DEBUG_ENABLED) {
            logMessage(null, Level.DEBUG, getCallerFrame(), *objects)
        }
    }

    actual fun info(vararg objects: Any) {
        if (IS_INFO_ENABLED) {
            logMessage(null, Level.INFO, getCallerFrame(), *objects)
        }
    }

    actual fun warn(vararg objects: Any) {
        if (IS_WARN_ENABLED) {
            logMessage(null, Level.WARN, getCallerFrame(), *objects)
        }
    }

    actual fun error(vararg objects: Any) {
        if (IS_ERROR_ENABLED) {
            logMessage(null, Level.ERROR, getCallerFrame(), *objects)
        }
    }

    actual fun d(tag: String, vararg arguments: String) {
        if (IS_DEBUG_ENABLED) {
            logMessage(tag, Level.DEBUG, getCallerFrame(), *arguments)
        }
    }

    actual fun i(tag: String, vararg arguments: String) {
        if (IS_INFO_ENABLED) {
            logMessage(tag, Level.INFO, getCallerFrame(), *arguments)
        }
    }

    actual fun e(tag: String, vararg arguments: String) {
        if (IS_ERROR_ENABLED) {
            logMessage(tag, Level.ERROR, getCallerFrame(), *arguments)
        }
    }

    private fun logMessage(
        tag: String?,
        level: Level,
        callerFrame: StackTraceElement,
        vararg objs: Any,
    ) {
        val pid = Process.myPid()
        val tid = Process.myTid()
        synchronized(lock) {
            val threadName = Thread.currentThread().name
            val threadPriority = Process.getThreadPriority(tid)
            val className = getCallerClassName(callerFrame)
            msgSB.clear()
            msgSB.append(pid)
            msgSB.append(" ")
            msgSB.append(tid)
            msgSB.append(" daspay ")
            msgSB.append(", v = ${appVersion ?: "NA"} ")
            msgSB.append("[")
            msgSB.append(threadName)
            msgSB.append(",pri=")
            msgSB.append(threadPriority)
            msgSB.append("]")
            msgSB.append(" ")
            msgSB.append(className)
            tag?.let {
                msgSB.append(tag)
                msgSB.append(" ")
            }
            msgSB.append(formatMessage(className, *objs))

            logLine(level, msgSB.toString())
            logger.log(level, msgSB.toString())
        }
    }

    private fun getCallerFrame(): StackTraceElement {
        val stack = Thread.currentThread().stackTrace
        if (stack.size < 3) {
            throw (RuntimeException("Stack size should be more than 4"))
        }
        return stack[4]
    }

    private fun getCallerClassName(caller: StackTraceElement): String {
        if (CHECK_LOCK && !Thread.holdsLock(lock)) {
            throw IllegalAccessException("logMessage(): not holding lock")
        }
        val sb = headerSb
        sb.clear()
        var file = caller.fileName
        if (file != null) {
            val pos: Int =
                if (file.contains(".kt")) {
                    file.indexOf(".kt")
                } else {
                    file.indexOf(".java")
                }
            if (pos > 0) {
                file = file.substring(0, pos)
            }
            sb.append(file)
            val line = caller.lineNumber
            if (line >= 0) {
                sb.append(':')
                sb.append(line)
            }
            sb.append(": ")
        }

        return sb.toString()
    }

    private fun formatMessage(className: String, vararg arguments: Any): String {
        if (CHECK_LOCK && !Thread.holdsLock(lock)) {
            throw IllegalAccessException("AppLogger.logMessage(): not holding lock")
        }
        var numObjs: Int
        if (arguments.size.also { numObjs = it } == 0) return ""

        val sb = headerSb
        sb.clear()

        var delim = false
        for (j in 0 until numObjs) {
            val o: Any = arguments[j]
            if (delim) sb.append('\n') else delim = true

            when (o) {
                is Throwable -> {
                    handleThrowable(o, sb)
                }

                is Class<*> -> {
                    val name = getSimpleName(o)
                    if (j != 0 || name != className) {
                        sb.append(name)
                        sb.append(": ")
                    }
                    delim = false
                }

                else -> {
                    prepareLogLine(o, sb)
                }
            }
        }
        return sb.toString()
    }

    private fun prepareLogLine(o: Any, sb: StringBuilder) {
        val lines = o.toString().split("\n").toTypedArray()
        val num = lines.size
        for (i in 0 until num) {
            if (i > 0) sb.append('\n')

            val line = lines[i]
            val length = line.length
            var start = 0
            var end = length
            while (start < length && line[start] == '\r') {
                ++start
            }
            while (end > start && line[end - 1] == '\r') {
                --end
            }
            if (start != 0 || end != length) {
                sb.append(line.substring(start, end))
            } else {
                sb.append(line)
            }
        }
    }

    private fun handleThrowable(throwable: Throwable, sb: StringBuilder) {
        val errorMessage = throwable.message
        sb.append(throwable::class.simpleName ?: "")
        if (errorMessage != null) {
            sb.append(": ")
            sb.append(errorMessage)
        }
        sb.append("\n")
        for (ste in throwable.stackTrace) {
            sb.append("  ")
            sb.append(ste.toString())
            sb.append("\n")
        }
        sb.append("caused by:\n")
    }

    private fun logLine(level: Level, line: String) {
        when {
            level === Level.DEBUG -> Log.d(TAG, line)
            level === Level.ERROR || level === Level.FATAL -> Log.e(TAG, line)
            level === Level.INFO -> Log.i(TAG, line)
            level === Level.WARN -> Log.w(TAG, line)
        }
    }

    private fun getSimpleName(cls: Class<*>): String? {
        var name = cls.simpleName
        if (name.isEmpty()) {
            name = cls.name
            val dot = name.lastIndexOf('.')
            if (dot != -1) {
                name = name.substring(dot + 1)
            }
        }
        return name
    }

    fun fatal(vararg arrayOfObjects: Any) {
        if (IS_ERROR_ENABLED) {
            logMessage(null, Level.FATAL, getCallerFrame(), *arrayOfObjects)
        }
    }
}

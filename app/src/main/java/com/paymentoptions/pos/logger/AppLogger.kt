package com.paymentoptions.pos.logger

import android.content.Context
import android.os.Process
import android.util.Log
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException

object AppLogger {
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

    /**
     * Init logger
     *
     * @param context
     * @param config
     */
    @Suppress("MaxLineLength")
    fun init(
        context: Context,
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

    /**
     * Init logger
     *
     * @param logLevel = when sdk does not wants to record any logs in file
     */
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

    /**
     * Configures the Log4j appender, layout pattern and sets the log file path.
     *
     * @param context Application context
     */
    private fun configureLogger(context: Context, logFolderPath: String?, logLevel: Level) {
        val rootPath = logFolderPath ?: context.cacheDir.absolutePath
        AppLogger.debug("SDK: Log Root Path $rootPath")
        val cfg = LogConfigurator(rootPath + File.separator + "Log" + File.separator + FILE_NAME)
        try {
            cfg.configureFileAppender(logger, logLevel)
            debug("Configured Log4j Successfully, file = ${cfg.fileName} ")
        } catch (e: IOException) {
            AppLogger.error("Error configuring log4J at ${cfg.fileName}: ", e)
        }
    }

    /**
     * Writes a DEBUG log message.
     *
     * @param objects list of object
     */
    fun debug(vararg objects: Any) {
        if (IS_DEBUG_ENABLED) {
            logMessage(null, Level.DEBUG, getCallerFrame(), *objects)
        }
    }

    /**
     * Writes a INFO log message.
     *
     * @param arguments of messages
     */
    fun info(vararg arguments: Any) {
        if (IS_INFO_ENABLED) {
            logMessage(null, Level.INFO, getCallerFrame(), *arguments)
        }
    }

    /**
     * Writes a WARN log message.
     *
     * @param arguments of messages
     */
    fun warn(vararg arguments: Any) {
        if (IS_WARN_ENABLED) {
            logMessage(null, Level.WARN, getCallerFrame(), *arguments)
        }
    }

    /**
     * Writes a ERROR log message.
     *
     * @param arguments of messages
     */
    fun error(vararg arguments: Any) {
        if (IS_ERROR_ENABLED) {
            logMessage(null, Level.ERROR, getCallerFrame(), *arguments)
        }
    }

    /**
     * Writes a DEBUG log message along with custom tag
     *
     * @param tag custom log tag
     * @param arguments of messages
     */
    fun d(tag: String, vararg arguments: String) {
        if (IS_DEBUG_ENABLED) {
            logMessage(tag, Level.DEBUG, getCallerFrame(), *arguments)
        }
    }

    /**
     * Writes a INFO log message along with custom tag
     *
     * @param tag custom log tag
     * @param arguments of messages
     */
    fun i(tag: String, vararg arguments: String) {
        if (IS_INFO_ENABLED) {
            logMessage(tag, Level.INFO, getCallerFrame(), *arguments)
        }
    }

    /**
     * Writes a ERROR log message along with custom tag
     *
     * @param tag custom log tag
     * @param arguments of messages
     */
    fun e(tag: String, vararg arguments: String) {
        if (IS_ERROR_ENABLED) {
            logMessage(tag, Level.ERROR, getCallerFrame(), *arguments)
        }
    }

    /**
     * Prepares the log message which consists of pid, thread id, class name along with line number
     * and log message with supported tag.
     */
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

            // Log to studio console
            logLine(level, msgSB.toString())
            // Log to file system using Log4j
            logger.log(level, msgSB.toString())
        }
    }

    /** This method returns the stack trace frames from where the log is generated. */
    private fun getCallerFrame(): StackTraceElement {
        val stack = Thread.currentThread().stackTrace
        if (stack.size < 3) {
            throw (RuntimeException("Stack size should be more than 4"))
        }
        return stack[4]
    }

    /**
     * Returns the class name and line number from the provided [StackTraceElement]
     *
     * @param caller stacktrace frame
     */
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

    /**
     * Splits the list of messages into a [StringBuilder]
     *
     * @param arguments of log messages.
     */
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
                    // ignore initial class name if same as filename
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
            // skip any leading and trailing CRs
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
        sb.append(throwable.javaClass.name ?: "")
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

    /**
     * Prints the log message into android studio console.
     *
     * @param level log priority level
     * @param line log message
     */
    private fun logLine(level: Level, line: String) {
        when {
            level === Level.DEBUG -> {
                Log.d(TAG, line)
            }

            level === Level.ERROR || level === Level.FATAL -> {
                Log.d(TAG, line)
            }

            level === Level.INFO -> {
                Log.i(TAG, line)
            }

            level === Level.WARN -> {
                Log.w(TAG, line)
            }
        }
    }

    /** Returns simple class name */
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

    /** Logs FATAL exceptions */
    fun fatal(vararg arrayOfObjects: Any) {
        if (IS_ERROR_ENABLED) {
            logMessage(null, Level.FATAL, getCallerFrame(), *arrayOfObjects)
        }
    }
}

package com.paymentoptions.pos.logger

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.RollingFileAppender
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.layout.PatternLayout

/**
 * This class configures the Logger. The log configuration is based on the Log4J Apache library. It
 * uses the RollingFileAppender with PatternLayout to print logs into files. Rolling of the file
 * happens once a file reaches to provided size. The max number of files is defined by
 * [MAX_BACKUP_SIZE_DEBUG]. Default size is 9. Once the number of files reaches to
 * [MAX_BACKUP_SIZE_DEBUG], it will rollover the old logs.
 */
class LogConfigurator(val fileName: String = "android-log4j.log") {
    /**
     * This method configures the [RollingFileAppender]
     *
     * @param logger [Logger] class to which the appended is added.
     */
    fun configureFileAppender(logger: Logger, logLevel: Level) {
        /* Set the logging level. Debug means it will support all the log levels*/
        Configurator.setRootLevel(logLevel)
        val rollingFileAppender: RollingFileAppender
        val fileLayout: Layout<*> = PatternLayout.newBuilder().withPattern(logPattern).build()
        // Default roll over policy
        val rolloverStrategy =
            DefaultRolloverStrategy.newBuilder()
                .withMax(if (logLevel == Level.DEBUG) MAX_BACKUP_SIZE_DEBUG else MAX_BACKUP_SIZE)
                .build()
        // Size policy
        val maxFileSize = if (logLevel == Level.DEBUG) MAX_FILE_SIZE_DEBUG else MAX_FILE_SIZE
        val sizePolicy = SizeBasedTriggeringPolicy.createPolicy("$maxFileSize")
        rollingFileAppender =
            RollingFileAppender.newBuilder()
                .setName("LogcatAppender")
                .withFileName("$fileName.log")
                .withFilePattern("$fileName.%d{yyyy-MM-dd}.%i.log")
                .setImmediateFlush(true)
                .setLayout(fileLayout)
                .withPolicy(sizePolicy)
                .withAppend(true)
                .withStrategy(rolloverStrategy)
                .build()
        rollingFileAppender.start()
        val context = LoggerContext.getContext(false)
        val config = context.configuration
        config.addAppender(rollingFileAppender)
        val rootLogger = logger as org.apache.logging.log4j.core.Logger
        // Stops printing to console. We are already printing in AppLogger class.
        rootLogger.isAdditive = false
        rootLogger.addAppender(rollingFileAppender)
    }

    companion object {
        private var MAX_FILE_SIZE = 5 * 1024L * 1024L
        private var MAX_FILE_SIZE_DEBUG = 10 * 1024L * 1024L
        private var MAX_BACKUP_SIZE: String = "3"
        private var MAX_BACKUP_SIZE_DEBUG: String = "9"
        private const val logPattern = "%d{dd-MM-yyyy HH:mm:ss:SSS} %-5p %m%n"
    }
}

package com.paymentoptions.pos.logger

import org.apache.logging.log4j.Level

data class Config(val logConfig: LogConfig? = null)

data class LogConfig(
    val logFilePrefix: String,
    val logFolderPath: String,
    val logLevel: Level = Level.DEBUG,
)

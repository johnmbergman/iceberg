package com.johnmbergman.iceberg.monitoring

interface Logger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
}
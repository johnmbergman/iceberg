package com.johnmbergman.iceberg.monitoring

import java.io.PrintStream
import kotlin.time.Clock

/**
 * A simple logger used for logging messages to a console.
 */
class SimpleLogger(
    val outStream: PrintStream = System.out,
    val errStream: PrintStream = System.err,
    val clock: Clock = Clock.System,
) : Logger {

    override fun debug(message: String) {
        outStream.println("[DEBUG] [${clock.now()}] $message")
    }

    override fun info(message: String) {
        outStream.println("[INFO] [${clock.now()}] $message")
    }

    override fun warn(message: String) {
        outStream.println("[WARN] [${clock.now()}] $message")
    }

    override fun error(message: String) {
        errStream.println("[ERROR] [${clock.now()}] $message")
    }
}
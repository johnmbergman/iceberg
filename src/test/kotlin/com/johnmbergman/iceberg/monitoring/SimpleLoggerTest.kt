package com.johnmbergman.iceberg.monitoring

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class SimpleLoggerTest {

    private val fixedClock = object : Clock {
        override fun now() = Instant.parse("2026-01-02T03:04:05Z")
    }
    private val newline = System.lineSeparator()

    private lateinit var out: ByteArrayOutputStream
    private lateinit var err: ByteArrayOutputStream
    private lateinit var logger: SimpleLogger

    @BeforeTest
    fun setUp() {
        out = ByteArrayOutputStream()
        err = ByteArrayOutputStream()
        logger = SimpleLogger(PrintStream(out, true), PrintStream(err, true), fixedClock)
    }

    @Test
    fun debugInfoAndWarnGoToStandardOut() {
        logger.debug("one")
        logger.info("two")
        logger.warn("three")

        assertEquals(
            "[DEBUG] [2026-01-02T03:04:05Z] one$newline" +
                "[INFO] [2026-01-02T03:04:05Z] two$newline" +
                "[WARN] [2026-01-02T03:04:05Z] three$newline",
            out.toString()
        )
        assertEquals("", err.toString())
    }

    @Test
    fun errorGoesToStandardError() {
        logger.error("boom")

        assertEquals("[ERROR] [2026-01-02T03:04:05Z] boom$newline", err.toString())
        assertEquals("", out.toString())
    }
}

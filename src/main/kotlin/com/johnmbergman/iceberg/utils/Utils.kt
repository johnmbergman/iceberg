package com.johnmbergman.iceberg.utils

import java.net.URL

object Utils {

    fun readResource(path: String): String {
        return resourceUrl(path).readText()
    }

    fun readResourceBytes(path: String): ByteArray {
        return resourceUrl(path).readBytes()
    }

    private fun resourceUrl(path: String): URL {
        val normalized = if (path.startsWith("/")) path else "/$path"
        return Utils::class.java.getResource(normalized)
            ?: throw IllegalArgumentException("Resource not found on classpath: $normalized")
    }
}
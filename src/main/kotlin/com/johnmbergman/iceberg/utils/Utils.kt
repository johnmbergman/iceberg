package com.johnmbergman.iceberg.utils

object Utils {

    fun readResource(path: String): String {
        val normalized = if (path.startsWith("/")) path else "/$path"
        return Utils::class.java.getResource(normalized)?.readText()
            ?: throw IllegalArgumentException("Resource not found on classpath: $normalized")
    }
}
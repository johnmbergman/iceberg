package com.johnmbergman.iceberg.math

data class Vector2(val x: Float, val y: Float) {
    companion object {
        val ZERO = Vector2(0f, 0f)
    }

    operator fun plus(other: Vector2): Vector2 {
        return Vector2(x + other.x, y + other.y)
    }
}

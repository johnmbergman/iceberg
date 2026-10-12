package com.johnmbergman.iceberg.math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class Vector2Test {

    @Test
    fun testVectorZeroValue() {
        assertEquals(0.0f, Vector2.ZERO.x)
        assertEquals(0.0f, Vector2.ZERO.y)
    }

    @Test
    fun testAddVector() {
        val a = Vector2(1.25f, 9.5f)
        val b = Vector2(2.45f, 8.6f)
        val c = a + b
        assertEquals(3.70f, c.x)
        assertEquals(18.1f, c.y)
    }
}
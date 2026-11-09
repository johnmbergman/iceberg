package com.johnmbergman.iceberg

import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.test.assertTrue

/** Float tolerance for comparing results of trigonometry and matrix math. */
const val TOLERANCE = 1e-5f

fun assertVectorEquals(expected: Vector3fc, actual: Vector3fc, tolerance: Float = TOLERANCE) {
    assertTrue(Vector3f(actual).equals(expected, tolerance), "Expected $expected but was $actual")
}

fun assertVectorEquals(x: Float, y: Float, z: Float, actual: Vector3fc, tolerance: Float = TOLERANCE) {
    assertVectorEquals(Vector3f(x, y, z), actual, tolerance)
}

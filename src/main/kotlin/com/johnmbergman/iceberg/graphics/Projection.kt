package com.johnmbergman.iceberg.graphics

import org.joml.Matrix4f

private const val DEFAULT_FOV = 1.0471976f // Approximately 60 degrees.
private const val Z_FAR = 1000f
private const val Z_NEAR = 0.1f

class Projection(private val width: Int = 0, private val height: Int = 0) {
    val projectionMatrix = Matrix4f()

    init {
        updateProjectionMatrix(width, height)
    }

    fun updateProjectionMatrix(width: Int, height: Int) {
        projectionMatrix.setPerspective(DEFAULT_FOV, width.toFloat() / height.toFloat(), Z_NEAR, Z_FAR)
    }
}
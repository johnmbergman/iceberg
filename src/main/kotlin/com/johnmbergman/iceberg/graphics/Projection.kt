package com.johnmbergman.iceberg.graphics

import org.joml.Matrix4f

private const val DEFAULT_FOV = 1.0471976f // Approximately 60 degrees.
private const val Z_FAR = 1000f
private const val Z_NEAR = 0.1f

/**
 * Perspective projection for a viewport of the given size. Until it is given a non-empty size,
 * the matrix is the identity.
 */
class Projection(width: Int = 0, height: Int = 0) {
    val projectionMatrix = Matrix4f()

    init {
        updateProjectionMatrix(width, height)
    }

    /**
     * Recomputes the matrix for the new viewport size. An empty size is ignored and the previous matrix kept:
     * minimizing a window can report a 0×0 framebuffer, and its aspect ratio (0 / 0) would make the matrix NaN.
     */
    fun updateProjectionMatrix(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            return
        }
        projectionMatrix.setPerspective(DEFAULT_FOV, width.toFloat() / height.toFloat(), Z_NEAR, Z_FAR)
    }
}

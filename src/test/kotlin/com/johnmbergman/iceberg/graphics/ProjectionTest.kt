package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.TOLERANCE
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * For a perspective matrix, m11 = 1 / tan(fovY / 2) and m00 = m11 / aspect,
 * so the field of view and aspect ratio can be read back from those two entries.
 */
class ProjectionTest {

    @Test
    fun usesSixtyDegreeVerticalFieldOfView() {
        val matrix = Projection(800, 600).projectionMatrix
        assertEquals(sqrt(3f), matrix.m11(), TOLERANCE) // 1 / tan(30°)
    }

    @Test
    fun constructorSizeSetsAspectRatio() {
        val matrix = Projection(800, 600).projectionMatrix
        assertEquals(800f / 600f, matrix.m11() / matrix.m00(), TOLERANCE)
    }

    @Test
    fun updateChangesAspectRatio() {
        val projection = Projection(800, 600)
        projection.updateProjectionMatrix(1920, 1080)

        val matrix = projection.projectionMatrix
        assertEquals(1920f / 1080f, matrix.m11() / matrix.m00(), TOLERANCE)
    }

    @Test
    fun aspectRatioDoesNotChangeVerticalFieldOfView() {
        val projection = Projection(800, 600)
        val before = projection.projectionMatrix.m11()
        projection.updateProjectionMatrix(1920, 1080)

        assertEquals(before, projection.projectionMatrix.m11(), TOLERANCE)
    }
}

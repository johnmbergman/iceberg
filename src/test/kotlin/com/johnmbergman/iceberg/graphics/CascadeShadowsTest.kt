package com.johnmbergman.iceberg.graphics

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val NEAR = 0.1f
private const val FAR = 1000f
private const val FOV = 1.0471976f

class CascadeShadowsTest {

    private val projection = Matrix4f().setPerspective(FOV, 4f / 3f, NEAR, FAR)

    private fun update(
        lightDirection: Vector3f = Vector3f(1f, 1f, 0f),
        view: Matrix4f = Matrix4f(),
    ): CascadeShadows = CascadeShadows().apply { update(view, projection, NEAR, FAR, lightDirection) }

    @Test
    fun `split distances move away from the camera and the last ends at the far plane`() {
        val cascades = update().cascades

        assertEquals(SHADOW_MAP_CASCADE_COUNT, cascades.size)
        var previous = -NEAR
        cascades.forEach {
            assertTrue(it.splitDistance < previous, "Split ${it.splitDistance} should be beyond $previous")
            previous = it.splitDistance
        }
        assertEquals(-FAR, cascades.last().splitDistance, 1e-2f)
    }

    @Test
    fun `split distances are mostly logarithmic`() {
        // lambda = 0.95 between logarithmic (2.15, 46.4, 1000) and uniform (333.4, 666.7, 1000) splits
        val splits = update().cascades.map { -it.splitDistance }

        assertEquals(18.71f, splits[0], 0.05f)
        assertEquals(77.43f, splits[1], 0.05f)
    }

    @Test
    fun `each cascade's shadow map covers its slice of the camera frustum`() {
        val view = Matrix4f().lookAt(0f, 2f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)
        val inverseView = Matrix4f(view).invert()
        val cascades = update(Vector3f(0.3f, 1f, -0.2f), view).cascades
        val tanHalfY = kotlin.math.tan(FOV / 2f)
        val tanHalfX = tanHalfY * 4f / 3f

        var sliceStart = NEAR
        cascades.forEach { cascade ->
            val sliceEnd = -cascade.splitDistance
            for (depth in listOf(sliceStart, sliceEnd)) {
                for (sx in listOf(-1f, 1f)) for (sy in listOf(-1f, 1f)) {
                    // A frustum corner at this depth, in view space then world space
                    val corner = Vector4f(sx * tanHalfX * depth, sy * tanHalfY * depth, -depth, 1f).mul(inverseView)
                    val clip = corner.mul(cascade.projectionViewMatrix)
                    val epsilon = 1e-3f
                    assertTrue(clip.x in -1f - epsilon..1f + epsilon, "x ${clip.x} outside cascade")
                    assertTrue(clip.y in -1f - epsilon..1f + epsilon, "y ${clip.y} outside cascade")
                    // zZeroToOne projection
                    assertTrue(clip.z in -epsilon..1f + epsilon, "z ${clip.z} outside cascade")
                }
            }
            sliceStart = sliceEnd
        }
    }

    @Test
    fun `a light straight overhead gives finite matrices`() {
        for (direction in listOf(Vector3f(0f, 1f, 0f), Vector3f(0f, -1f, 0f))) {
            update(direction).cascades.forEach {
                assertTrue(it.projectionViewMatrix.isFinite, "Matrix for light $direction is ${it.projectionViewMatrix}")
            }
        }
    }
}

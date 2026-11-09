package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.TOLERANCE
import com.johnmbergman.iceberg.assertVectorEquals
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val QUARTER_TURN = (PI / 2).toFloat()

/**
 * Conventions: the camera starts at the origin looking down -Z with +Y up.
 * Rotation x is pitch (positive looks down) and rotation y is yaw (positive turns right).
 */
class CameraTest {

    @Test
    fun startsAtOriginWithIdentityView() {
        val camera = Camera()
        assertVectorEquals(0f, 0f, 0f, camera.position)
        assertTrue(Matrix4f(camera.viewMatrix).equals(Matrix4f(), TOLERANCE))
    }

    @Test
    fun movesAlongItsOwnAxesWhenUnrotated() {
        val camera = Camera()

        camera.moveForward(2f)
        assertVectorEquals(0f, 0f, -2f, camera.position)
        camera.moveBackwards(2f)
        assertVectorEquals(0f, 0f, 0f, camera.position)

        camera.moveRight(3f)
        assertVectorEquals(3f, 0f, 0f, camera.position)
        camera.moveLeft(3f)
        assertVectorEquals(0f, 0f, 0f, camera.position)

        camera.moveUp(4f)
        assertVectorEquals(0f, 4f, 0f, camera.position)
        camera.moveDown(4f)
        assertVectorEquals(0f, 0f, 0f, camera.position)
    }

    @Test
    fun positiveYawTurnsRight() {
        val camera = Camera()
        camera.addRotation(0f, QUARTER_TURN)

        camera.moveForward(1f)
        assertVectorEquals(1f, 0f, 0f, camera.position)
    }

    @Test
    fun positivePitchLooksDown() {
        val camera = Camera()
        camera.addRotation(QUARTER_TURN, 0f)

        camera.moveForward(1f)
        assertVectorEquals(0f, -1f, 0f, camera.position)
    }

    @Test
    fun moveUpIsRelativeToTheCameraNotTheWorld() {
        val camera = Camera()
        camera.addRotation(QUARTER_TURN, 0f) // Looking straight down, the top of the screen faces -Z

        camera.moveUp(1f)
        assertVectorEquals(0f, 0f, -1f, camera.position)
    }

    @Test
    fun viewMatrixMovesCameraPositionToOrigin() {
        val camera = Camera()
        camera.setPosition(1f, 2f, 3f)

        val viewSpace = camera.viewMatrix.transformPosition(Vector3f(1f, 2f, 3f))
        assertVectorEquals(0f, 0f, 0f, viewSpace)
    }

    @Test
    fun viewMatrixPutsPointsAheadOfTheCameraOnNegativeZ() {
        val camera = Camera()
        camera.setPosition(5f, 0f, 0f)
        camera.setRotation(0f, QUARTER_TURN) // Facing +X

        val viewSpace = camera.viewMatrix.transformPosition(Vector3f(7f, 0f, 0f))
        assertVectorEquals(0f, 0f, -2f, viewSpace)
    }

    @Test
    fun addRotationAccumulatesAndSetRotationReplaces() {
        val camera = Camera()

        camera.addRotation(0.1f, 0.2f)
        camera.addRotation(0.1f, 0.2f)
        assertEquals(0.2f, camera.rotation.x(), TOLERANCE)
        assertEquals(0.4f, camera.rotation.y(), TOLERANCE)

        camera.setRotation(0.5f, 0.6f)
        assertEquals(0.5f, camera.rotation.x(), TOLERANCE)
        assertEquals(0.6f, camera.rotation.y(), TOLERANCE)
    }
}

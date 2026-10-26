package com.johnmbergman.iceberg.scene

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc

class Camera {
    val position: Vector3fc field = Vector3f()
    val rotation: Vector2fc field = Vector2f()
    val viewMatrix: Matrix4fc field = Matrix4f()

    private val direction = Vector3f()
    private val right = Vector3f()
    private val up = Vector3f()

    fun addRotation(x: Float, y: Float) {
        rotation.add(x, y)
        recalculate()
    }

    fun moveBackwards(distance: Float) {
        viewMatrix.positiveZ(direction).negate().mul(distance)
        position.sub(direction)
        recalculate()
    }

    fun moveDown(distance: Float) {
        viewMatrix.positiveY(up).mul(distance)
        position.sub(up)
        recalculate()
    }

    fun moveForward(distance: Float) {
        viewMatrix.positiveZ(direction).negate().mul(distance)
        position.add(direction)
        recalculate()
    }

    fun moveLeft(distance: Float) {
        viewMatrix.positiveX(right).mul(distance)
        position.sub(right)
        recalculate()
    }

    fun moveRight(distance: Float) {
        viewMatrix.positiveX(right).mul(distance)
        position.add(right)
        recalculate()
    }

    fun moveUp(distance: Float) {
        viewMatrix.positiveY(up).mul(distance)
        position.add(up)
        recalculate()
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        position.set(x, y, z)
        recalculate()
    }

    fun setRotation(x: Float, y: Float) {
        rotation.set(x, y)
        recalculate()
    }

    private fun recalculate() {
        viewMatrix.identity()
            .rotateX(rotation.x)
            .rotateY(rotation.y)
            .translate(-position.x, -position.y, -position.z)
    }
}

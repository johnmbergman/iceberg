package com.johnmbergman.iceberg.scene

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class Entity(val id: String, val modelId: String) {

    // Since these are public they can be externally modified (not ideal)
    val modelMatrix = Matrix4f()
    val position = Vector3f()
    val rotation = Quaternionf()

    var scale = 1f

    fun setPosition(x: Float, y: Float, z: Float) {
        position.set(x, y, z)
    }

    fun setRotation(x: Float, y: Float, z: Float, angleInRadians: Float) {
        rotation.fromAxisAngleRad(x, y, z, angleInRadians)
    }
    
    fun updateModelMatrix() {
        modelMatrix.translationRotateScale(position, rotation, scale)
    }
}
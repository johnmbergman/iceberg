package com.johnmbergman.iceberg.graphics

import org.joml.Vector4f

class Material(val texture: Texture) {
    val meshList = mutableListOf<Mesh>()

    var diffuseColor: Vector4f = DEFAULT_COLOR

    fun cleanup() {
        meshList.forEach { it.cleanup() }
    }

    companion object {
        val DEFAULT_COLOR = Vector4f(0f, 0f, 0f, 1f)
    }
}
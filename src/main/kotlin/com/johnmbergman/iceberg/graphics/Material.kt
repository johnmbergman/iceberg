package com.johnmbergman.iceberg.graphics

import org.joml.Vector4f

class Material(val texture: Texture) {
    val meshList = mutableListOf<Mesh>()

    var normalMapPath: String = DEFAULT_NORMAL_MAP_PATH
    var ambientColor: Vector4f = DEFAULT_COLOR
    var diffuseColor: Vector4f = DEFAULT_COLOR
    var specularColor: Vector4f = DEFAULT_COLOR
    var reflectance: Float = 0f

    fun cleanup() {
        meshList.forEach { it.cleanup() }
    }

    companion object {
        val DEFAULT_COLOR = Vector4f(0f, 0f, 0f, 1f)
        const val DEFAULT_NORMAL_MAP_PATH = ""
    }
}
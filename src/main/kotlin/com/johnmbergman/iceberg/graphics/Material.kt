package com.johnmbergman.iceberg.graphics

import org.joml.Vector4f
import org.joml.Vector4fc

class Material(val texture: Texture) {
    val meshList = mutableListOf<Mesh>()

    var normalMapPath: String = DEFAULT_NORMAL_MAP_PATH
    var reflectance: Float = 0f

    // Each material owns its colors; the public properties are read-only views of them.
    private val ambient = Vector4f(DEFAULT_COLOR)
    private val diffuse = Vector4f(DEFAULT_COLOR)
    private val specular = Vector4f(DEFAULT_COLOR)

    var ambientColor: Vector4fc
        get() = ambient
        set(value) { ambient.set(value) }

    var diffuseColor: Vector4fc
        get() = diffuse
        set(value) { diffuse.set(value) }

    var specularColor: Vector4fc
        get() = specular
        set(value) { specular.set(value) }

    fun cleanup() {
        meshList.forEach { it.cleanup() }
    }

    companion object {
        private val DEFAULT_COLOR: Vector4fc = Vector4f(0f, 0f, 0f, 1f)
        const val DEFAULT_NORMAL_MAP_PATH = ""
    }
}

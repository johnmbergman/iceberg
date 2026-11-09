package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.math.Color

class Material(val texture: Texture) {
    val meshList = mutableListOf<Mesh>()

    var normalMapPath: String = DEFAULT_NORMAL_MAP_PATH
    var reflectance: Float = 0f

    // Colors are immutable, so materials can safely share them, including the default
    var ambientColor: Color = DEFAULT_COLOR
    var diffuseColor: Color = DEFAULT_COLOR
    var specularColor: Color = DEFAULT_COLOR

    fun cleanup() {
        meshList.forEach { it.cleanup() }
    }

    companion object {
        /** Black, because scene.frag adds material colors to the texture color; black adds nothing to RGB. */
        private val DEFAULT_COLOR = Color.BLACK
        const val DEFAULT_NORMAL_MAP_PATH = ""
    }
}

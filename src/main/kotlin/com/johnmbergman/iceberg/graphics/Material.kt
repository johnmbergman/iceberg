package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.assets.MaterialData
import com.johnmbergman.iceberg.math.Color

/** A material on the GPU. [normalMap] is null when the material has none. */
class Material(val texture: Texture, val normalMap: Texture? = null) {
    val meshList = mutableListOf<Mesh>()

    var reflectance: Float = 0f

    // Colors are immutable, so materials can safely share them, including the default
    var ambientColor: Color = MaterialData.DEFAULT_COLOR
    var diffuseColor: Color = MaterialData.DEFAULT_COLOR
    var specularColor: Color = MaterialData.DEFAULT_COLOR

    fun cleanup() {
        meshList.forEach { it.cleanup() }
    }
}

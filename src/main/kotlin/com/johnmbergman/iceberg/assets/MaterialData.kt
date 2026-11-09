package com.johnmbergman.iceberg.assets

import com.johnmbergman.iceberg.math.Color

/**
 * A material as read from a model file, with the meshes that use it. Textures are classpath resource paths;
 * null means the material has none.
 */
class MaterialData(
    val diffuseTexturePath: String? = null,
    val normalMapPath: String? = null,
    val ambientColor: Color = DEFAULT_COLOR,
    val diffuseColor: Color = DEFAULT_COLOR,
    val specularColor: Color = DEFAULT_COLOR,
    val reflectance: Float = 0f,
    val meshes: List<MeshData> = emptyList(),
) {
    companion object {
        /** Black, because scene.frag adds material colors to the texture color; black adds nothing to RGB. */
        val DEFAULT_COLOR = Color.BLACK
    }
}

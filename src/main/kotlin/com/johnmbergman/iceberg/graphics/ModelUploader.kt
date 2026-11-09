package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.assets.MaterialData
import com.johnmbergman.iceberg.assets.ModelData

/** Turns [ModelData] into GPU resources. Requires a current OpenGL context; [ResourceCache] calls it. */
object ModelUploader {

    fun upload(data: ModelData, textureCache: TextureCache): Model =
        Model(data.id, data.materials.map { upload(it, textureCache) })

    private fun upload(data: MaterialData, textureCache: TextureCache): Material {
        val material = Material(
            texture = data.diffuseTexturePath?.let { textureCache[it] } ?: textureCache.default,
            normalMap = data.normalMapPath?.let { textureCache[it] },
        )
        material.ambientColor = data.ambientColor
        material.diffuseColor = data.diffuseColor
        material.specularColor = data.specularColor
        material.reflectance = data.reflectance
        data.meshes.forEach { mesh ->
            material.meshList += Mesh(
                mesh.positions, mesh.normals, mesh.tangents, mesh.bitangents, mesh.textureCoordinates, mesh.indices
            )
        }
        return material
    }
}

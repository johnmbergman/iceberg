package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.graphics.Model
import com.johnmbergman.iceberg.graphics.TextureCache

class SkyBox(skyBoxModelPath: String, textureCache: TextureCache) {
    val skyBoxModel: Model = ModelLoader.loadModel("skybox-model", skyBoxModelPath, textureCache)
    val skyBoxEntity: Entity = Entity("skybox-entity", skyBoxModel.id)

    fun cleanup() {
        skyBoxModel.cleanup()
    }
}

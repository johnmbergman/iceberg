package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.assets.ModelLoader
import com.johnmbergman.iceberg.scene.ModelId

/**
 * Owns the GPU resources loaded from asset files: textures and models. Game code loads models through it and
 * stores the returned [ModelId]s on scene nodes; renderers turn ids back into [Model]s.
 *
 * Lives in `graphics` rather than `assets` because it owns GPU objects. Requires a current OpenGL context.
 */
class ResourceCache {
    val textures = TextureCache()
    private val models = mutableMapOf<ModelId, Model>()

    /**
     * Loads and uploads a model from a classpath resource, once. Later calls with the same path return the
     * same id without reloading, so [flags] only apply to the first load of a path.
     */
    fun loadModel(path: String, flags: Int = ModelLoader.DEFAULT_FLAGS): ModelId {
        val id = ModelId(path.removePrefix("/"))
        if (id !in models) {
            models[id] = ModelUploader.upload(ModelLoader.load(id.path, id.path, flags), textures)
        }
        return id
    }

    fun model(id: ModelId): Model =
        models[id] ?: throw IllegalArgumentException("No model loaded for $id; load it with ResourceCache.loadModel first")

    /** Frees models before textures; both must go while the OpenGL context still exists. */
    fun cleanup() {
        models.values.forEach { it.cleanup() }
        models.clear()
        textures.cleanup()
    }
}

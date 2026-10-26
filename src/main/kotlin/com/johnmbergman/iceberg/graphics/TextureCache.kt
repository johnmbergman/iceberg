package com.johnmbergman.iceberg.graphics

/**
 * Loads each texture file once and owns the resulting textures.
 * Must be created and used while an OpenGL context is active.
 */
class TextureCache {

    private val textureMap = mutableMapOf<String, Texture>()

    /** Used by materials that don't specify a texture. */
    val default: Texture = get(DEFAULT_TEXTURE_PATH)

    /**
     * Returns the texture for this classpath resource, lazily loading it on first use.
     */
    operator fun get(path: String) : Texture {
        require(path.isNotBlank()) { "Texture path must not be blank" }
        return textureMap.getOrPut(path) { TextureLoader.load(path) }
    }

    fun cleanup() {
        textureMap.values.forEach { it.cleanup() }
        textureMap.clear()
    }

    companion object {
        const val DEFAULT_TEXTURE_PATH = "models/default/default_texture.png"
    }
}

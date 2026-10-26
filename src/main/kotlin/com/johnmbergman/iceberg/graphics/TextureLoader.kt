package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.utils.Utils
import org.lwjgl.stb.STBImage.stbi_failure_reason
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

object TextureLoader {

    fun load(resourcePath: String): Texture {
        val bytes = Utils.readResourceBytes(resourcePath)
        val encoded = MemoryUtil.memAlloc(bytes.size).put(bytes).flip()
        try {
            MemoryStack.stackPush().use { stack ->
                val width = stack.mallocInt(1)
                val height = stack.mallocInt(1)
                val channels = stack.mallocInt(1)
                val pixels = stbi_load_from_memory(encoded, width, height, channels, 4)
                    ?: throw IllegalStateException("Image [$resourcePath] could not be loaded: ${stbi_failure_reason()}")

                try {
                    return Texture(width[0], height[0], pixels)
                } finally {
                    stbi_image_free(pixels)
                }
            }
        } finally {
            MemoryUtil.memFree(encoded)
        }
    }
}
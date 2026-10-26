package com.johnmbergman.iceberg.graphics

import org.lwjgl.opengl.GL11C.GL_NEAREST
import org.lwjgl.opengl.GL11C.GL_RGBA
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11C.GL_UNPACK_ALIGNMENT
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL11C.glBindTexture
import org.lwjgl.opengl.GL11C.glDeleteTextures
import org.lwjgl.opengl.GL11C.glGenTextures
import org.lwjgl.opengl.GL11C.glPixelStorei
import org.lwjgl.opengl.GL11C.glTexImage2D
import org.lwjgl.opengl.GL11C.glTexParameteri
import org.lwjgl.opengl.GL30C.glGenerateMipmap
import java.nio.ByteBuffer

class Texture(val width: Int, val height: Int, buffer: ByteBuffer) {
    private val id = glGenTextures()

    init {
        glBindTexture(GL_TEXTURE_2D, id)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glGenerateMipmap(GL_TEXTURE_2D)
    }

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun cleanup() {
        glDeleteTextures(id)
    }
}
package com.johnmbergman.iceberg.graphics

import org.lwjgl.opengl.GL11C.GL_FLOAT
import org.lwjgl.opengl.GL11C.GL_LINEAR
import org.lwjgl.opengl.GL11C.GL_NONE
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S
import org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T
import org.lwjgl.opengl.GL11C.GL_DEPTH_COMPONENT
import org.lwjgl.opengl.GL11C.glBindTexture
import org.lwjgl.opengl.GL11C.glDeleteTextures
import org.lwjgl.opengl.GL11C.glDrawBuffer
import org.lwjgl.opengl.GL11C.glGenTextures
import org.lwjgl.opengl.GL11C.glReadBuffer
import org.lwjgl.opengl.GL11C.glTexParameteri
import org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL12C.glTexImage3D
import org.lwjgl.opengl.GL14C.GL_TEXTURE_COMPARE_MODE
import org.lwjgl.opengl.GL30C.GL_DEPTH_ATTACHMENT
import org.lwjgl.opengl.GL30C.GL_DEPTH_COMPONENT32F
import org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER_COMPLETE
import org.lwjgl.opengl.GL30C.GL_TEXTURE_2D_ARRAY
import org.lwjgl.opengl.GL30C.glBindFramebuffer
import org.lwjgl.opengl.GL30C.glCheckFramebufferStatus
import org.lwjgl.opengl.GL30C.glDeleteFramebuffers
import org.lwjgl.opengl.GL30C.glFramebufferTextureLayer
import org.lwjgl.opengl.GL30C.glGenFramebuffers
import java.nio.ByteBuffer

// Each cascade's shadow map is SHADOW_MAP_SIZE × SHADOW_MAP_SIZE texels
internal const val SHADOW_MAP_SIZE = 4096

/**
 * A depth-only framebuffer and the shadow maps it renders into: one layer of a 2D texture array per cascade.
 * Requires a current OpenGL context.
 *
 * A texture array, rather than one texture per cascade, lets the scene shader pick a cascade per fragment: GLSL
 * only allows indexing an array of samplers with a constant (or, from 4.0, dynamically uniform) expression.
 */
class ShadowBuffer {
    private val framebufferId = glGenFramebuffers()
    private val depthTextureId = glGenTextures()

    init {
        glBindTexture(GL_TEXTURE_2D_ARRAY, depthTextureId)
        glTexImage3D(
            GL_TEXTURE_2D_ARRAY, 0, GL_DEPTH_COMPONENT32F,
            SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, SHADOW_MAP_CASCADE_COUNT,
            0, GL_DEPTH_COMPONENT, GL_FLOAT, null as ByteBuffer?
        )
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        // The shader compares depths itself, so sampling returns the raw stored depth
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_COMPARE_MODE, GL_NONE)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0)

        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId)
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTextureId, 0, 0)
        // Depth only: no color buffer to draw into or read from
        glDrawBuffer(GL_NONE)
        glReadBuffer(GL_NONE)
        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Could not create the shadow framebuffer (status 0x${status.toString(16)})")
        }
    }

    /** Binds the framebuffer and makes cascade [layer]'s shadow map its depth attachment. */
    fun bindForWriting(layer: Int) {
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId)
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTextureId, 0, layer)
    }

    /** Binds the shadow maps to the active texture unit. */
    fun bindTexture() {
        glBindTexture(GL_TEXTURE_2D_ARRAY, depthTextureId)
    }

    fun cleanup() {
        glDeleteFramebuffers(framebufferId)
        glDeleteTextures(depthTextureId)
    }
}

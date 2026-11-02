package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.Window
import imgui.ImDrawData
import imgui.ImGui
import imgui.ImVec4
import imgui.type.ImInt
import org.joml.Vector2f
import org.lwjgl.opengl.GL11C.GL_BLEND
import org.lwjgl.opengl.GL11C.GL_CULL_FACE
import org.lwjgl.opengl.GL11C.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11C.GL_LINEAR
import org.lwjgl.opengl.GL11C.GL_ONE
import org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11C.GL_RGBA
import org.lwjgl.opengl.GL11C.GL_SCISSOR_TEST
import org.lwjgl.opengl.GL11C.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11C.GL_TRIANGLES
import org.lwjgl.opengl.GL11C.GL_UNPACK_ALIGNMENT
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_SHORT
import org.lwjgl.opengl.GL11C.glBindTexture
import org.lwjgl.opengl.GL11C.glDeleteTextures
import org.lwjgl.opengl.GL11C.glDisable
import org.lwjgl.opengl.GL11C.glDrawElements
import org.lwjgl.opengl.GL11C.glEnable
import org.lwjgl.opengl.GL11C.glGenTextures
import org.lwjgl.opengl.GL11C.glIsEnabled
import org.lwjgl.opengl.GL11C.glPixelStorei
import org.lwjgl.opengl.GL11C.glScissor
import org.lwjgl.opengl.GL11C.glTexImage2D
import org.lwjgl.opengl.GL11C.glTexParameteri
import org.lwjgl.opengl.GL13C.GL_TEXTURE0
import org.lwjgl.opengl.GL13C.glActiveTexture
import org.lwjgl.opengl.GL14C.GL_FUNC_ADD
import org.lwjgl.opengl.GL14C.glBlendEquation
import org.lwjgl.opengl.GL14C.glBlendFuncSeparate
import org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER
import org.lwjgl.opengl.GL15C.GL_STREAM_DRAW
import org.lwjgl.opengl.GL15C.glBindBuffer
import org.lwjgl.opengl.GL15C.glBufferData
import org.lwjgl.opengl.GL30C.glBindVertexArray

private const val SCALE_UNIFORM_NAME = "scale"
private const val TRANSLATE_UNIFORM_NAME = "translate"
private const val TEXTURE_SAMPLER_UNIFORM_NAME = "textureSampler"

/**
 * Draws the ImGui draw data produced by [com.johnmbergman.iceberg.ui.UserInterface.endFrame].
 * The ImGui context must exist before this is constructed, since the font atlas is built here.
 */
class UIRenderer {
    private val shaderProgram: ShaderProgram
    private val uniforms: UniformsMap
    private val uiMesh: UIMesh
    private val fontTextureId: Int

    private val scale = Vector2f()
    private val translate = Vector2f()
    private val clipRectangle = ImVec4()

    constructor() {
        val shaderModuleDataList = listOf(
            ShaderModuleData("shaders/ui.vert", ShaderType.VERTEX),
            ShaderModuleData("shaders/ui.frag", ShaderType.FRAGMENT)
        )
        shaderProgram = ShaderProgram(shaderModuleDataList)

        uniforms = UniformsMap(shaderProgram.programId)
        uniforms.createUniform(SCALE_UNIFORM_NAME)
        uniforms.createUniform(TRANSLATE_UNIFORM_NAME)
        uniforms.createUniform(TEXTURE_SAMPLER_UNIFORM_NAME)

        uiMesh = UIMesh()
        fontTextureId = createFontTexture()
    }

    // ImGui.newFrame() asserts unless the font atlas has been built and uploaded first
    private fun createFontTexture(): Int {
        val fontAtlas = ImGui.getIO().fonts
        val width = ImInt()
        val height = ImInt()
        val pixels = fontAtlas.getTexDataAsRGBA32(width, height)

        val textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureId)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
        // Text is drawn at arbitrary sub-pixel positions, so filter linearly and skip mipmaps
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glBindTexture(GL_TEXTURE_2D, 0)

        // Draw commands report this ID back so the right texture can be bound per command
        fontAtlas.setTexID(textureId.toLong())
        return textureId
    }

    fun cleanup() {
        uiMesh.cleanup()
        glDeleteTextures(fontTextureId)
        shaderProgram.cleanup()
    }

    fun render(window: Window) {
        val drawData = ImGui.getDrawData()
        if (drawData.cmdListsCount == 0) {
            return
        }

        // Remember the scene's state so the UI pass doesn't leak into the next frame
        val wasBlendEnabled = glIsEnabled(GL_BLEND)
        val wasCullFaceEnabled = glIsEnabled(GL_CULL_FACE)
        val wasDepthTestEnabled = glIsEnabled(GL_DEPTH_TEST)
        val wasScissorTestEnabled = glIsEnabled(GL_SCISSOR_TEST)

        glEnable(GL_BLEND)
        glBlendEquation(GL_FUNC_ADD)
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_CULL_FACE)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_SCISSOR_TEST)

        shaderProgram.bind()
        // Draw data is in screen coordinates, not framebuffer pixels; map that rectangle to clip space
        val displayX = drawData.displayPosX
        val displayY = drawData.displayPosY
        scale.set(2.0f / drawData.displaySizeX, -2.0f / drawData.displaySizeY)
        translate.set(-1.0f - displayX * scale.x, 1.0f - displayY * scale.y)
        uniforms.setUniform(SCALE_UNIFORM_NAME, scale)
        uniforms.setUniform(TRANSLATE_UNIFORM_NAME, translate)
        uniforms.setUniform(TEXTURE_SAMPLER_UNIFORM_NAME, 0)
        glActiveTexture(GL_TEXTURE0)

        glBindVertexArray(uiMesh.vertexArrayObjectId)
        glBindBuffer(GL_ARRAY_BUFFER, uiMesh.verticesVertexBufferObject)
        // The element buffer binding is part of the VAO state, so bind it while the VAO is bound
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, uiMesh.indicesVertexBufferObject)

        val indexSize = ImDrawData.sizeOfImDrawIdx()
        val indexType = if (indexSize == 2) GL_UNSIGNED_SHORT else GL_UNSIGNED_INT
        // Clip rectangles are in screen coordinates; glScissor wants framebuffer pixels from the bottom-left
        val framebufferScaleX = drawData.framebufferScaleX
        val framebufferScaleY = drawData.framebufferScaleY
        val framebufferHeight = window.height

        for (commandListIndex in 0 until drawData.cmdListsCount) {
            glBufferData(GL_ARRAY_BUFFER, drawData.getCmdListVtxBufferData(commandListIndex), GL_STREAM_DRAW)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, drawData.getCmdListIdxBufferData(commandListIndex), GL_STREAM_DRAW)

            for (commandIndex in 0 until drawData.getCmdListCmdBufferSize(commandListIndex)) {
                drawData.getCmdListCmdBufferClipRect(clipRectangle, commandListIndex, commandIndex)
                val clipMinX = (clipRectangle.x - displayX) * framebufferScaleX
                val clipMinY = (clipRectangle.y - displayY) * framebufferScaleY
                val clipMaxX = (clipRectangle.z - displayX) * framebufferScaleX
                val clipMaxY = (clipRectangle.w - displayY) * framebufferScaleY
                if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) {
                    continue
                }
                glScissor(
                    clipMinX.toInt(),
                    (framebufferHeight - clipMaxY).toInt(),
                    (clipMaxX - clipMinX).toInt(),
                    (clipMaxY - clipMinY).toInt()
                )

                glBindTexture(GL_TEXTURE_2D, drawData.getCmdListCmdBufferTextureId(commandListIndex, commandIndex).toInt())
                // RendererHasVtxOffset isn't set, so ImGui keeps every vertex offset at 0 and only index offsets apply
                val elementCount = drawData.getCmdListCmdBufferElemCount(commandListIndex, commandIndex)
                val indexOffset = drawData.getCmdListCmdBufferIdxOffset(commandListIndex, commandIndex)
                glDrawElements(GL_TRIANGLES, elementCount, indexType, indexOffset.toLong() * indexSize)
            }
        }

        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindTexture(GL_TEXTURE_2D, 0)
        shaderProgram.unbind()

        setEnabled(GL_BLEND, wasBlendEnabled)
        setEnabled(GL_CULL_FACE, wasCullFaceEnabled)
        setEnabled(GL_DEPTH_TEST, wasDepthTestEnabled)
        setEnabled(GL_SCISSOR_TEST, wasScissorTestEnabled)
    }

    private fun setEnabled(capability: Int, enabled: Boolean) {
        if (enabled) glEnable(capability) else glDisable(capability)
    }
}

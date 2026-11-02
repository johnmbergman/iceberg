package com.johnmbergman.iceberg.graphics

import imgui.ImDrawData
import org.lwjgl.opengl.GL11C.GL_FLOAT
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15C.glBindBuffer
import org.lwjgl.opengl.GL15C.glDeleteBuffers
import org.lwjgl.opengl.GL15C.glGenBuffers
import org.lwjgl.opengl.GL20C.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20C.glVertexAttribPointer
import org.lwjgl.opengl.GL30C.glBindVertexArray
import org.lwjgl.opengl.GL30C.glDeleteVertexArrays
import org.lwjgl.opengl.GL30C.glGenVertexArrays

class UIMesh {
    val indicesVertexBufferObject: Int
    val vertexArrayObjectId: Int
    val verticesVertexBufferObject: Int

    init {
        vertexArrayObjectId = glGenVertexArrays()
        glBindVertexArray(vertexArrayObjectId)

        // Single Vertex Buffer Object (VBO)
        verticesVertexBufferObject = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, verticesVertexBufferObject)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, ImDrawData.sizeOfImDrawVert(), 0)
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, ImDrawData.sizeOfImDrawVert(), 8)
        glEnableVertexAttribArray(2)
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, ImDrawData.sizeOfImDrawVert(), 16)

        indicesVertexBufferObject = glGenBuffers()

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun cleanup() {
        glDeleteBuffers(indicesVertexBufferObject)
        glDeleteBuffers(verticesVertexBufferObject)
        glDeleteVertexArrays(vertexArrayObjectId)
    }
}
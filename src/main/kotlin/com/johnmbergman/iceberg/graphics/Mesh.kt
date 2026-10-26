package com.johnmbergman.iceberg.graphics

import org.lwjgl.opengl.GL11C.GL_FLOAT
import org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER
import org.lwjgl.opengl.GL15C.GL_STATIC_DRAW
import org.lwjgl.opengl.GL15C.glBindBuffer
import org.lwjgl.opengl.GL15C.glBufferData
import org.lwjgl.opengl.GL15C.glGenBuffers
import org.lwjgl.opengl.GL20C.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20C.glVertexAttribPointer
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30C.glBindVertexArray
import org.lwjgl.opengl.GL30C.glDeleteVertexArrays
import org.lwjgl.opengl.GL30C.glGenVertexArrays
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Mesh {
    val numberOfVertices: Int
    val vertexArrayObjectId: Int
    val vertexBufferObjectIdList: MutableList<Int>

    constructor(positions: FloatArray, textureCoordinates: FloatArray, indices: IntArray) {
        this.numberOfVertices = indices.size

        vertexBufferObjectIdList = mutableListOf()
        vertexArrayObjectId = glGenVertexArrays()
        glBindVertexArray(vertexArrayObjectId)

        // Positions VBO
        val positionsVboId = glGenBuffers()
        vertexBufferObjectIdList.add(positionsVboId)
        val positionsBuffer: FloatBuffer = MemoryUtil.memCallocFloat(positions.size)
        positionsBuffer.put(0, positions)
        glBindBuffer(GL_ARRAY_BUFFER, positionsVboId);
        glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // Texture coordinates VBO
        val textureCoordinatesVboId = glGenBuffers()
        vertexBufferObjectIdList.add(textureCoordinatesVboId)
        val textureCoordinatesBuffer: FloatBuffer = MemoryUtil.memCallocFloat(textureCoordinates.size)
        textureCoordinatesBuffer.put(0, textureCoordinates)
        glBindBuffer(GL_ARRAY_BUFFER, textureCoordinatesVboId);
        glBufferData(GL_ARRAY_BUFFER, textureCoordinatesBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        // Index VBO
        val indicesVboId = glGenBuffers()
        vertexBufferObjectIdList.add(indicesVboId)
        val indicesBuffer: IntBuffer = MemoryUtil.memCallocInt(indices.size)
        indicesBuffer.put(0, indices)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        MemoryUtil.memFree(positionsBuffer)
        MemoryUtil.memFree(textureCoordinatesBuffer)
        MemoryUtil.memFree(indicesBuffer)
    }

    fun cleanup() {
        vertexBufferObjectIdList.forEach(GL30::glDeleteBuffers)
        glDeleteVertexArrays(vertexArrayObjectId)
    }
}

package com.johnmbergman.iceberg.graphics

import org.lwjgl.opengl.GL11C.GL_FLOAT
import org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER
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

class Mesh {
    val numberOfVertices: Int
    val vertexArrayObjectId: Int
    val vertexBufferObjectIdList: MutableList<Int>

    constructor(positions: FloatArray, numberOfVertices: Int) {
        this.numberOfVertices = numberOfVertices

        vertexBufferObjectIdList = mutableListOf()
        vertexArrayObjectId = glGenVertexArrays()
        glBindVertexArray(vertexArrayObjectId)

        // Positions VBO
        val vboId = glGenBuffers()
        vertexBufferObjectIdList.add(vboId)
        val positionsBuffer: FloatBuffer = MemoryUtil.memCallocFloat(positions.size)
        positionsBuffer.put(0, positions)
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)

        MemoryUtil.memFree(positionsBuffer)
    }

    fun cleanup() {
        vertexBufferObjectIdList.forEach(GL30::glDeleteBuffers)
        glDeleteVertexArrays(vertexArrayObjectId)
    }
}

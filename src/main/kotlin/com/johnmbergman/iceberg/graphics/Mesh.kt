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

class Mesh(
    positions: FloatArray,
    normals: FloatArray,
    tangents: FloatArray,
    bitangents: FloatArray,
    textureCoordinates: FloatArray,
    indices: IntArray,
) {
    val numberOfVertices = indices.size
    val vertexArrayObjectId = glGenVertexArrays()
    private val vertexBufferObjectIdList: MutableList<Int> = mutableListOf()

    init {
        glBindVertexArray(vertexArrayObjectId)

        createAttributeBuffer(0, 3, positions)
        createAttributeBuffer(1, 3, normals)
        createAttributeBuffer(2, 3, tangents)
        createAttributeBuffer(3, 3, bitangents)
        createAttributeBuffer(4, 2, textureCoordinates)
        createIndexBuffer(indices)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    private fun createAttributeBuffer(index: Int, componentsPerVertex: Int, data: FloatArray) {
        val id = glGenBuffers().also(vertexBufferObjectIdList::add)
        glBindBuffer(GL_ARRAY_BUFFER, id)
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        glEnableVertexAttribArray(index)
        glVertexAttribPointer(index, componentsPerVertex, GL_FLOAT, false, 0, 0)
    }

    private fun createIndexBuffer(indices: IntArray) {
        val id = glGenBuffers().also(vertexBufferObjectIdList::add)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
    }

    fun cleanup() {
        vertexBufferObjectIdList.forEach(GL30::glDeleteBuffers)
        glDeleteVertexArrays(vertexArrayObjectId)
    }
}

package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.scene.Scene
import org.lwjgl.opengl.GL11C.GL_TRIANGLES
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL11C.glDrawElements
import org.lwjgl.opengl.GL30C.glBindVertexArray

private const val PROJECTION_MATRIX_UNIFORM_NAME = "projectionMatrix"

class SceneRenderer {
    private val shaderProgram: ShaderProgram
    private val uniforms: UniformsMap

    constructor() {
        val shaderModuleDataList = listOf(
            ShaderModuleData("shaders/scene.vert", ShaderType.VERTEX),
            ShaderModuleData("shaders/scene.frag", ShaderType.FRAGMENT)
        )
        shaderProgram = ShaderProgram(shaderModuleDataList)
        uniforms = UniformsMap(shaderProgram.programId)
        uniforms.createUniform(PROJECTION_MATRIX_UNIFORM_NAME)
    }

    fun cleanup() {
        shaderProgram.cleanup()
    }

    fun render(scene: Scene) {
        shaderProgram.bind()
        uniforms.setUniform(PROJECTION_MATRIX_UNIFORM_NAME, scene.projection.projectionMatrix)
        scene.meshMap.forEach { (_, mesh) ->
            glBindVertexArray(mesh.vertexArrayObjectId)
            glDrawElements(GL_TRIANGLES, mesh.numberOfVertices, GL_UNSIGNED_INT, 0)
        }
        glBindVertexArray(0)
        shaderProgram.unbind()
    }
}
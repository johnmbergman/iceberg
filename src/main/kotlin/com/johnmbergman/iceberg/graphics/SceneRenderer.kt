package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.scene.Scene
import org.lwjgl.opengl.GL11C.GL_TRIANGLES
import org.lwjgl.opengl.GL11C.glDrawArrays
import org.lwjgl.opengl.GL30C.glBindVertexArray

class SceneRenderer {
    private val shaderProgram: ShaderProgram

    constructor() {
        val shaderModuleDataList = listOf(
            ShaderModuleData("shaders/scene.vert", ShaderType.VERTEX),
            ShaderModuleData("shaders/scene.frag", ShaderType.FRAGMENT)
        )
        shaderProgram = ShaderProgram(shaderModuleDataList)
    }

    fun cleanup() {
        shaderProgram.cleanup()
    }

    fun render(scene: Scene) {
        shaderProgram.bind()
        scene.meshMap.forEach { (_, mesh) ->
            glBindVertexArray(mesh.vertexArrayObjectId);
            glDrawArrays(GL_TRIANGLES, 0, mesh.numberOfVertices);
        }
        glBindVertexArray(0)
        shaderProgram.unbind()
    }
}
package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.scene.Scene
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11C.GL_TRIANGLES
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL11C.glDrawElements
import org.lwjgl.opengl.GL13C.GL_TEXTURE0
import org.lwjgl.opengl.GL13C.glActiveTexture
import org.lwjgl.opengl.GL30C.glBindVertexArray

private const val PROJECTION_MATRIX_UNIFORM_NAME = "projectionMatrix"
private const val VIEW_MATRIX_UNIFORM_NAME = "viewMatrix"
private const val MODEL_MATRIX_UNIFORM_NAME = "modelMatrix"
private const val DIFFUSE_UNIFORM_NAME = "diffuse"
private const val TEXTURE_SAMPLER_UNIFORM_NAME = "textureSampler"
private const val HAS_TEXTURE_UNIFORM_NAME = "hasTexture"

/**
 * Draws the scene's skybox, if it has one. Run it before [SceneRenderer]: the skybox ignores the
 * camera's translation, so it always surrounds the viewer and anything drawn afterwards appears in front.
 */
class SkyBoxRenderer(private val textureCache: TextureCache) {
    private val shaderProgram: ShaderProgram
    private val uniforms: UniformsMap

    // Camera view matrix with its translation stripped, reused each frame
    private val viewMatrix = Matrix4f()

    init {
        val shaderModuleDataList = listOf(
            ShaderModuleData("shaders/skybox.vert", ShaderType.VERTEX),
            ShaderModuleData("shaders/skybox.frag", ShaderType.FRAGMENT)
        )
        shaderProgram = ShaderProgram(shaderModuleDataList)

        uniforms = UniformsMap(shaderProgram.programId)
        uniforms.createUniform(PROJECTION_MATRIX_UNIFORM_NAME)
        uniforms.createUniform(VIEW_MATRIX_UNIFORM_NAME)
        uniforms.createUniform(MODEL_MATRIX_UNIFORM_NAME)
        uniforms.createUniform(DIFFUSE_UNIFORM_NAME)
        uniforms.createUniform(TEXTURE_SAMPLER_UNIFORM_NAME)
        uniforms.createUniform(HAS_TEXTURE_UNIFORM_NAME)
    }

    fun cleanup() {
        shaderProgram.cleanup()
    }

    fun render(scene: Scene) {
        val skyBox = scene.skyBox ?: return
        shaderProgram.bind()

        uniforms.setUniform(PROJECTION_MATRIX_UNIFORM_NAME, scene.projection.projectionMatrix)
        // Keep only the camera's rotation so the skybox moves with the viewer
        viewMatrix.set(scene.camera.viewMatrix)
        viewMatrix.m30(0f)
        viewMatrix.m31(0f)
        viewMatrix.m32(0f)
        uniforms.setUniform(VIEW_MATRIX_UNIFORM_NAME, viewMatrix)
        uniforms.setUniform(MODEL_MATRIX_UNIFORM_NAME, skyBox.skyBoxEntity.modelMatrix)
        uniforms.setUniform(TEXTURE_SAMPLER_UNIFORM_NAME, 0)

        glActiveTexture(GL_TEXTURE0)
        val defaultTexture = textureCache.default
        skyBox.skyBoxModel.materialList.forEach { material ->
            material.texture.bind()
            uniforms.setUniform(DIFFUSE_UNIFORM_NAME, material.diffuseColor)
            uniforms.setUniform(HAS_TEXTURE_UNIFORM_NAME, if (material.texture === defaultTexture) 0 else 1)

            material.meshList.forEach { mesh ->
                glBindVertexArray(mesh.vertexArrayObjectId)
                glDrawElements(GL_TRIANGLES, mesh.numberOfVertices, GL_UNSIGNED_INT, 0)
            }
        }

        glBindVertexArray(0)
        shaderProgram.unbind()
    }
}

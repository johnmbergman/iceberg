package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.scene.lighting.DirectionalLight
import org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT
import org.lwjgl.opengl.GL11C.GL_TRIANGLES
import org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL11C.glClear
import org.lwjgl.opengl.GL11C.glDrawElements
import org.lwjgl.opengl.GL11C.glViewport
import org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30C.glBindFramebuffer
import org.lwjgl.opengl.GL30C.glBindVertexArray

private const val MODEL_MATRIX_UNIFORM_NAME = "modelMatrix"
private const val PROJECTION_VIEW_MATRIX_UNIFORM_NAME = "projViewMatrix"

/**
 * Renders the scene's depth from the directional light into one shadow map per cascade. Run it before
 * [SceneRenderer], which reads [shadowBuffer] and [cascadeShadows]. Leaves the default framebuffer bound, but
 * changes the viewport, so set it again afterwards.
 *
 * Only the first directional light casts shadows, matching the one [LightPacker] gives the scene shader. With no
 * directional light the maps are just cleared to the far plane, so nothing is in shadow.
 */
class ShadowRenderer {
    private val shaderProgram: ShaderProgram
    private val uniforms: UniformsMap

    val shadowBuffer = ShadowBuffer()
    val cascadeShadows = CascadeShadows()

    init {
        // Depth only, so no fragment shader
        shaderProgram = ShaderProgram(listOf(ShaderModuleData("shaders/shadow.vert", ShaderType.VERTEX)))
        uniforms = UniformsMap(shaderProgram.programId)
        uniforms.createUniform(MODEL_MATRIX_UNIFORM_NAME)
        uniforms.createUniform(PROJECTION_VIEW_MATRIX_UNIFORM_NAME)
    }

    fun cleanup() {
        shaderProgram.cleanup()
        shadowBuffer.cleanup()
    }

    fun render(scene: Scene, renderQueue: RenderQueue, resources: ResourceCache) {
        val light = firstDirectionalLight(scene)
        val camera = scene.camera
        if (light != null) {
            val projection = camera.projection
            cascadeShadows.update(
                camera.viewMatrix, projection.projectionMatrix, projection.near, projection.far, light.direction
            )
        }

        glViewport(0, 0, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE)
        shaderProgram.bind()
        val cascades = cascadeShadows.cascades
        for (i in cascades.indices) {
            shadowBuffer.bindForWriting(i)
            glClear(GL_DEPTH_BUFFER_BIT)
            if (light == null) continue

            uniforms.setUniform(PROJECTION_VIEW_MATRIX_UNIFORM_NAME, cascades[i].projectionViewMatrix)
            for ((modelId, worldMatrices) in renderQueue.batches) {
                resources.model(modelId).materialList.forEach { material ->
                    material.meshList.forEach { mesh ->
                        glBindVertexArray(mesh.vertexArrayObjectId)
                        worldMatrices.forEach { worldMatrix ->
                            uniforms.setUniform(MODEL_MATRIX_UNIFORM_NAME, worldMatrix)
                            glDrawElements(GL_TRIANGLES, mesh.numberOfVertices, GL_UNSIGNED_INT, 0)
                        }
                    }
                }
            }
        }
        glBindVertexArray(0)
        shaderProgram.unbind()
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    // Indexed so the per-frame lookup doesn't create an iterator, like LightPacker
    private fun firstDirectionalLight(scene: Scene): DirectionalLight? {
        val lights = scene.lights
        for (i in lights.indices) {
            (lights[i] as? DirectionalLight)?.let { return it }
        }
        return null
    }
}

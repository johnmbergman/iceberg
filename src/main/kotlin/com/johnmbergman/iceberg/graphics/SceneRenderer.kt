package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.scene.Fog
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.scene.lighting.DirectionalLight
import com.johnmbergman.iceberg.scene.lighting.PointLight
import com.johnmbergman.iceberg.scene.lighting.PositionalLight
import com.johnmbergman.iceberg.scene.lighting.SpotLight
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL13C.GL_TEXTURE0
import org.lwjgl.opengl.GL13C.GL_TEXTURE1
import org.lwjgl.opengl.GL13C.glActiveTexture
import org.lwjgl.opengl.GL14C.GL_FUNC_ADD
import org.lwjgl.opengl.GL14C.glBlendEquation
import org.lwjgl.opengl.GL30C.glBindVertexArray


private const val PROJECTION_MATRIX_UNIFORM_NAME = "projectionMatrix"
private const val VIEW_MATRIX_UNIFORM_NAME = "viewMatrix"
private const val MODEL_MATRIX_UNIFORM_NAME = "modelMatrix"
private const val NORMAL_MATRIX_UNIFORM_NAME = "normalMatrix"
private const val TEXTURE_SAMPLER_UNIFORM_NAME = "textureSampler"
private const val NORMAL_SAMPLER_UNIFORM_NAME = "normalSampler"
private const val MATERIAL_AMBIENT_UNIFORM_NAME = "material.ambient"
private const val MATERIAL_DIFFUSE_UNIFORM_NAME = "material.diffuse"
private const val MATERIAL_SPECULAR_UNIFORM_NAME = "material.specular"
private const val MATERIAL_REFLECTANCE_UNIFORM_NAME = "material.reflectance"
private const val MATERIAL_NORMAL_MAP_UNIFORM_NAME = "material.hasNormalMap"
private const val AMBIENT_LIGHT_UNIFORM_NAME = "ambientLight"
private const val POINT_LIGHTS_UNIFORM_NAME = "pointLights"
private const val SPOT_LIGHTS_UNIFORM_NAME = "spotLights"
private const val DIRECTIONAL_LIGHT_UNIFORM_NAME = "dirLight"

// Must match the array sizes in scene.frag
private const val MAX_POINT_LIGHTS = 5
private const val MAX_SPOT_LIGHTS = 5

class SceneRenderer(private val resources: ResourceCache) {
    private val shaderProgram: ShaderProgram
    private val uniforms: UniformsMap

    // Rebuilt every frame; kept so its lists are reused
    private val renderQueue = RenderQueue()

    // Scratch vector for converting light positions/directions to view space without allocating per frame
    private val viewSpaceVector = Vector3f()

    // Normal matrices for the batch being drawn, one per node, computed once and reused for every mesh. Both
    // the list and its matrices are reused between batches and frames so drawing doesn't allocate.
    private val normalMatrices = mutableListOf<Matrix3f>()
    private val modelViewMatrix = Matrix4f()

    init {
        val shaderModuleDataList = listOf(
            ShaderModuleData("shaders/scene.vert", ShaderType.VERTEX),
            ShaderModuleData("shaders/scene.frag", ShaderType.FRAGMENT)
        )
        shaderProgram = ShaderProgram(shaderModuleDataList)

        uniforms = UniformsMap(shaderProgram.programId)
        createUniforms()
    }

    private fun createUniforms() {
        uniforms.createUniform(PROJECTION_MATRIX_UNIFORM_NAME)
        uniforms.createUniform(VIEW_MATRIX_UNIFORM_NAME)
        uniforms.createUniform(MODEL_MATRIX_UNIFORM_NAME)
        uniforms.createUniform(NORMAL_MATRIX_UNIFORM_NAME)
        uniforms.createUniform(TEXTURE_SAMPLER_UNIFORM_NAME)
        uniforms.createUniform(NORMAL_SAMPLER_UNIFORM_NAME)

        uniforms.createUniform(MATERIAL_AMBIENT_UNIFORM_NAME)
        uniforms.createUniform(MATERIAL_DIFFUSE_UNIFORM_NAME)
        uniforms.createUniform(MATERIAL_SPECULAR_UNIFORM_NAME)
        uniforms.createUniform(MATERIAL_REFLECTANCE_UNIFORM_NAME)
        uniforms.createUniform(MATERIAL_NORMAL_MAP_UNIFORM_NAME)

        uniforms.createUniform("$AMBIENT_LIGHT_UNIFORM_NAME.factor")
        uniforms.createUniform("$AMBIENT_LIGHT_UNIFORM_NAME.color")

        for (i in 0..<MAX_POINT_LIGHTS) {
            createPointLightUniforms("$POINT_LIGHTS_UNIFORM_NAME[$i]")
        }
        for (i in 0..<MAX_SPOT_LIGHTS) {
            val name = "$SPOT_LIGHTS_UNIFORM_NAME[$i]"
            createPointLightUniforms("$name.pl")
            uniforms.createUniform("$name.conedir")
            uniforms.createUniform("$name.cutoff")
        }

        uniforms.createUniform("$DIRECTIONAL_LIGHT_UNIFORM_NAME.color")
        uniforms.createUniform("$DIRECTIONAL_LIGHT_UNIFORM_NAME.direction")
        uniforms.createUniform("$DIRECTIONAL_LIGHT_UNIFORM_NAME.intensity")

        // Fog uniforms
        uniforms.createUniform("fog.activeFog");
        uniforms.createUniform("fog.color");
        uniforms.createUniform("fog.density");
    }

    /** Creates the uniforms for a GLSL `PointLight` struct, which spot lights also embed as `pl`. */
    private fun createPointLightUniforms(name: String) {
        uniforms.createUniform("$name.position")
        uniforms.createUniform("$name.color")
        uniforms.createUniform("$name.intensity")
        uniforms.createUniform("$name.att.constant")
        uniforms.createUniform("$name.att.linear")
        uniforms.createUniform("$name.att.exponent")
    }

    fun cleanup() {
        shaderProgram.cleanup()
    }

    fun render(scene: Scene) {
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        shaderProgram.bind()
        uniforms.setUniform(PROJECTION_MATRIX_UNIFORM_NAME, scene.camera.projection.projectionMatrix)
        uniforms.setUniform(VIEW_MATRIX_UNIFORM_NAME, scene.camera.viewMatrix);
        uniforms.setUniform(TEXTURE_SAMPLER_UNIFORM_NAME, 0)
        uniforms.setUniform(NORMAL_SAMPLER_UNIFORM_NAME, 1)
        updateLights(scene)
        glActiveTexture(GL_TEXTURE0)

        val fog: Fog = scene.environment.fog
        uniforms.setUniform("fog.activeFog", if (fog.active) 1 else 0)
        uniforms.setUniformRgb("fog.color", fog.color)
        uniforms.setUniform("fog.density", fog.density)

        renderQueue.build(scene)
        for ((modelId, worldMatrices) in renderQueue.batches) {
            val model = resources.model(modelId)
            computeNormalMatrices(scene.camera.viewMatrix, worldMatrices)
            model.materialList.forEach { material ->
                uniforms.setUniform(MATERIAL_AMBIENT_UNIFORM_NAME, material.ambientColor)
                uniforms.setUniform(MATERIAL_DIFFUSE_UNIFORM_NAME, material.diffuseColor)
                uniforms.setUniform(MATERIAL_SPECULAR_UNIFORM_NAME, material.specularColor)
                uniforms.setUniform(MATERIAL_REFLECTANCE_UNIFORM_NAME, material.reflectance)
                uniforms.setUniform(MATERIAL_NORMAL_MAP_UNIFORM_NAME, if (material.normalMap != null) 1 else 0)
                // Bind a texture to unit 1 even without a normal map. The shader doesn't sample it then
                // (hasNormalMap is 0), but some drivers, such as macOS's, still warn when a sampler's unit
                // has no usable texture. The default texture is just a placeholder to keep them quiet.
                val normalMapTexture = material.normalMap ?: resources.textures.default
                glActiveTexture(GL_TEXTURE1)
                normalMapTexture.bind()

                glActiveTexture(GL_TEXTURE0)
                material.texture.bind()
                material.meshList.forEach { mesh ->
                    glBindVertexArray(mesh.vertexArrayObjectId)
                    worldMatrices.forEachIndexed { i, worldMatrix ->
                        uniforms.setUniform(MODEL_MATRIX_UNIFORM_NAME, worldMatrix)
                        uniforms.setUniform(NORMAL_MATRIX_UNIFORM_NAME, normalMatrices[i])
                        glDrawElements(GL_TRIANGLES, mesh.numberOfVertices, GL_UNSIGNED_INT, 0)
                    }
                }
            }
        }
        glBindVertexArray(0)
        shaderProgram.unbind()
        glDisable(GL_BLEND)
    }

    /** Fills [normalMatrices] with the inverse transpose of each node's model-view matrix, in batch order. */
    private fun computeNormalMatrices(viewMatrix: Matrix4fc, worldMatrices: List<Matrix4fc>) {
        while (normalMatrices.size < worldMatrices.size) {
            normalMatrices += Matrix3f()
        }
        worldMatrices.forEachIndexed { i, worldMatrix ->
            viewMatrix.mul(worldMatrix, modelViewMatrix).normal(normalMatrices[i])
        }
    }

    private fun updateLights(scene: Scene) {
        val viewMatrix = scene.camera.viewMatrix

        val environment = scene.environment
        uniforms.setUniform("$AMBIENT_LIGHT_UNIFORM_NAME.factor", environment.ambientIntensity)
        uniforms.setUniformRgb("$AMBIENT_LIGHT_UNIFORM_NAME.color", environment.ambientColor)

        // The shader supports a single directional light; any extras are ignored
        updateDirectionalLight(scene.lights.firstNotNullOfOrNull { it as? DirectionalLight }, viewMatrix)

        val pointLights = scene.lights.filterIsInstance<PointLight>()
        for (i in 0..<MAX_POINT_LIGHTS) {
            updatePointLight(pointLights.getOrNull(i), "$POINT_LIGHTS_UNIFORM_NAME[$i]", viewMatrix)
        }

        val spotLights = scene.lights.filterIsInstance<SpotLight>()
        for (i in 0..<MAX_SPOT_LIGHTS) {
            updateSpotLight(spotLights.getOrNull(i), "$SPOT_LIGHTS_UNIFORM_NAME[$i]", viewMatrix)
        }
    }

    /** A null light zeroes its uniforms so the shader treats the slot as off. */
    private fun updateDirectionalLight(light: DirectionalLight?, viewMatrix: Matrix4fc) {
        val name = DIRECTIONAL_LIGHT_UNIFORM_NAME
        if (light == null) {
            uniforms.setUniform("$name.intensity", 0f)
            return
        }
        viewMatrix.transformDirection(light.direction, viewSpaceVector)
        uniforms.setUniformRgb("$name.color", light.color)
        uniforms.setUniform("$name.direction", viewSpaceVector)
        uniforms.setUniform("$name.intensity", light.intensity)
    }

    /**
     * Sets the uniforms of a GLSL `PointLight` struct. Spot lights embed one as `pl`, so both
     * Kotlin light types share this. A null light zeroes the slot so the shader skips it.
     */
    private fun updatePointLight(light: PositionalLight?, name: String, viewMatrix: Matrix4fc) {
        if (light == null) {
            // The shader skips any slot whose intensity is 0
            uniforms.setUniform("$name.intensity", 0f)
            return
        }
        viewMatrix.transformPosition(light.position, viewSpaceVector)
        uniforms.setUniform("$name.position", viewSpaceVector)
        uniforms.setUniformRgb("$name.color", light.color)
        uniforms.setUniform("$name.intensity", light.intensity)
        uniforms.setUniform("$name.att.constant", light.attenuation.constant)
        uniforms.setUniform("$name.att.linear", light.attenuation.linear)
        uniforms.setUniform("$name.att.exponent", light.attenuation.exponent)
    }

    private fun updateSpotLight(light: SpotLight?, name: String, viewMatrix: Matrix4fc) {
        updatePointLight(light, "$name.pl", viewMatrix)
        if (light == null) return

        viewMatrix.transformDirection(light.direction, viewSpaceVector)
        uniforms.setUniform("$name.conedir", viewSpaceVector)
        uniforms.setUniform("$name.cutoff", light.cutoffCos)
    }
}

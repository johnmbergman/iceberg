package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.monitoring.SimpleLogger
import com.johnmbergman.iceberg.scene.Fog
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.scene.lighting.DirectionalLight
import com.johnmbergman.iceberg.scene.lighting.PositionalLight
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3fc
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL13C.GL_TEXTURE0
import org.lwjgl.opengl.GL13C.GL_TEXTURE1
import org.lwjgl.opengl.GL13C.GL_TEXTURE2
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
private const val SHADOW_MAP_UNIFORM_NAME = "shadowMap"
private const val CASCADE_SHADOWS_UNIFORM_NAME = "cascadeshadows"

// Texture units: 0 is the material's texture, 1 its normal map, 2 the shadow maps
private const val SHADOW_MAP_TEXTURE_UNIT = 2

class SceneRenderer(private val resources: ResourceCache) {
    private val shaderProgram: ShaderProgram
    private val uniforms: UniformsMap

    // Chooses the lights that fit in the shader's slots and converts them to view space
    private val lightPacker = LightPacker(SimpleLogger())

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

        uniforms.createUniform(SHADOW_MAP_UNIFORM_NAME)
        for (i in 0..<SHADOW_MAP_CASCADE_COUNT) {
            uniforms.createUniform("$CASCADE_SHADOWS_UNIFORM_NAME[$i].projViewMatrix")
            // The shader never reads the last cascade's split distance, so the compiler may drop that uniform
            if (i < SHADOW_MAP_CASCADE_COUNT - 1) {
                uniforms.createUniform("$CASCADE_SHADOWS_UNIFORM_NAME[$i].splitDistance")
            }
        }
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

    /** Draws [renderQueue], shadowed using the maps [shadowRenderer] rendered this frame. */
    fun render(scene: Scene, renderQueue: RenderQueue, shadowRenderer: ShadowRenderer) {
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

        updateShadows(shadowRenderer)
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

    private fun updateShadows(shadowRenderer: ShadowRenderer) {
        uniforms.setUniform(SHADOW_MAP_UNIFORM_NAME, SHADOW_MAP_TEXTURE_UNIT)
        val cascades = shadowRenderer.cascadeShadows.cascades
        for (i in cascades.indices) {
            val name = "$CASCADE_SHADOWS_UNIFORM_NAME[$i]"
            uniforms.setUniform("$name.projViewMatrix", cascades[i].projectionViewMatrix)
            if (i < cascades.size - 1) {
                uniforms.setUniform("$name.splitDistance", cascades[i].splitDistance)
            }
        }
        glActiveTexture(GL_TEXTURE2)
        shadowRenderer.shadowBuffer.bindTexture()
        glActiveTexture(GL_TEXTURE0)
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
        val environment = scene.environment
        uniforms.setUniform("$AMBIENT_LIGHT_UNIFORM_NAME.factor", environment.ambientIntensity)
        uniforms.setUniformRgb("$AMBIENT_LIGHT_UNIFORM_NAME.color", environment.ambientColor)

        val lights = lightPacker.pack(scene.lights, scene.camera.viewMatrix)
        setDirectionalLight(lights.directional, lights.directionalViewDirection)
        // Write every slot, including empty ones: uniforms keep their values, so a removed light would stay lit
        for (i in 0..<MAX_POINT_LIGHTS) {
            val packed = lights.pointLight(i)
            setPointLight("$POINT_LIGHTS_UNIFORM_NAME[$i]", packed?.light, packed?.viewPosition)
        }
        for (i in 0..<MAX_SPOT_LIGHTS) {
            setSpotLight("$SPOT_LIGHTS_UNIFORM_NAME[$i]", lights.spotLight(i))
        }
    }

    /** A null light zeroes its intensity so the shader treats the slot as off. */
    private fun setDirectionalLight(light: DirectionalLight?, viewDirection: Vector3fc) {
        val name = DIRECTIONAL_LIGHT_UNIFORM_NAME
        if (light == null) {
            uniforms.setUniform("$name.intensity", 0f)
            return
        }
        uniforms.setUniformRgb("$name.color", light.color)
        uniforms.setUniform("$name.direction", viewDirection)
        uniforms.setUniform("$name.intensity", light.intensity)
    }

    /**
     * Sets the uniforms of a GLSL `PointLight` struct. Spot lights embed one as `pl`, so both
     * Kotlin light types share this. A null light zeroes the slot so the shader skips it.
     */
    private fun setPointLight(name: String, light: PositionalLight?, viewPosition: Vector3fc?) {
        if (light == null || viewPosition == null) {
            // The shader skips any slot whose intensity is 0
            uniforms.setUniform("$name.intensity", 0f)
            return
        }
        uniforms.setUniform("$name.position", viewPosition)
        uniforms.setUniformRgb("$name.color", light.color)
        uniforms.setUniform("$name.intensity", light.intensity)
        uniforms.setUniform("$name.att.constant", light.attenuation.constant)
        uniforms.setUniform("$name.att.linear", light.attenuation.linear)
        uniforms.setUniform("$name.att.exponent", light.attenuation.exponent)
    }

    private fun setSpotLight(name: String, packed: PackedSpotLight?) {
        setPointLight("$name.pl", packed?.light, packed?.viewPosition)
        if (packed == null) return

        uniforms.setUniform("$name.conedir", packed.viewDirection)
        uniforms.setUniform("$name.cutoff", packed.light.cutoffCos)
    }
}

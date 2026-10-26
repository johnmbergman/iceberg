package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.Material
import com.johnmbergman.iceberg.graphics.Mesh
import com.johnmbergman.iceberg.graphics.Model
import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.scene.Entity
import com.johnmbergman.iceberg.scene.Scene
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW.GLFW_KEY_A
import org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN
import org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
import org.lwjgl.glfw.GLFW.GLFW_KEY_Q
import org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT
import org.lwjgl.glfw.GLFW.GLFW_KEY_UP
import org.lwjgl.glfw.GLFW.GLFW_KEY_X
import org.lwjgl.glfw.GLFW.GLFW_KEY_Z
import kotlin.floatArrayOf

val TEST_QUAD = Mesh(
    floatArrayOf(
        -0.5f, 0.5f, -5f,
        -0.5f, -0.5f, -5f,
        0.5f, -0.5f, -5f,
        0.5f, 0.5f, -5f,
        ),
    floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
        ),
    intArrayOf(0, 1, 3, 3, 1, 2)
)

/**
 * Each face has its own four vertices so it can map the full texture.
 * Faces are wound counter-clockwise when viewed from outside the cube.
 */
val TEST_CUBE = Mesh(
    floatArrayOf(
        // Front face
        -0.5f, 0.5f, 0.5f,
        -0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        // Back face
        0.5f, 0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        -0.5f, 0.5f, -0.5f,
        // Right face
        0.5f, 0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        // Left face
        -0.5f, 0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f,
        // Top face
        -0.5f, 0.5f, -0.5f,
        -0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, 0.5f,
        0.5f, 0.5f, -0.5f,
        // Bottom face
        -0.5f, -0.5f, 0.5f,
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, 0.5f,
    ),
    floatArrayOf(
        // Front face
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        // Back face
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        // Right face
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        // Left face
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        // Top face
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
        // Bottom face
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
    ),
    intArrayOf(
        // Front face
        0, 1, 2, 2, 3, 0,
        // Back face
        4, 5, 6, 6, 7, 4,
        // Right face
        8, 9, 10, 10, 11, 8,
        // Left face
        12, 13, 14, 14, 15, 12,
        // Top face
        16, 17, 18, 18, 19, 16,
        // Bottom face
        20, 21, 22, 22, 23, 20,
    )
)

class TestGame: IAppLogic {

    private val displayInc = Vector4f()
    private lateinit var cubeEntity: Entity
    private var rotation = 0.toDouble()

    fun start() {
        Engine(this).start()
    }

    override fun onInit(window: Window, scene: Scene, renderer: Renderer) {
        val cubeModelId = "cube-model"
        val cubeMaterial = Material(scene.textureCache.default)
        cubeMaterial.meshList.add(TEST_CUBE)
        val cubeModel = Model(cubeModelId, listOf(cubeMaterial))
        scene.addModel(cubeModel)

        cubeEntity = Entity("cube-entity", cubeModelId)
        cubeEntity.setPosition(0f, 0f, -2f)
        scene.addEntity(cubeEntity)
    }

    override fun onInput(window: Window, scene: Scene, deltaMillis: Long) {
        displayInc.zero()
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            displayInc.y = 1.0f
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            displayInc.y = -1.0f
        }

        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            displayInc.x = -1.0f
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            displayInc.x = 1.0f
        }

        if (window.isKeyPressed(GLFW_KEY_A)) {
            displayInc.z = -1.0f
        } else if (window.isKeyPressed(GLFW_KEY_Q)) {
            displayInc.z = 1.0f
        }

        if (window.isKeyPressed(GLFW_KEY_Z)) {
            displayInc.w = -1.0f
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            displayInc.w = 1.0f
        }

        displayInc.mul(deltaMillis / 1_000.0f)
        val entityPosition = cubeEntity.position
        cubeEntity.setPosition(
            displayInc.x + entityPosition.x,
            displayInc.y + entityPosition.y,
            displayInc.z + entityPosition.z)
        cubeEntity.scale += displayInc.w
        cubeEntity.updateModelMatrix()
    }

    override fun onUpdate(window: Window, scene: Scene, deltaMillis: Long) {
        rotation += 1.5f
        if (rotation > 360) {
            rotation = 0.0
        }
        cubeEntity.setRotation(1f, 1f, 1f, Math.toRadians(rotation).toFloat())
    }
}
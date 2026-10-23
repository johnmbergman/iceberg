package com.johnmbergman.iceberg

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
        0.5f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f,
        0.0f, 0.0f, 0.5f,
        0.0f, 0.5f, 0.5f,
        ),
    intArrayOf(0, 1, 3, 3, 1, 2)
)

val TEST_CUBE = Mesh(
    floatArrayOf(
        // VO
        -0.5f, 0.5f, 0.5f,
        // V1
        -0.5f, -0.5f, 0.5f,
        // V2
        0.5f, -0.5f, 0.5f,
        // V3
        0.5f, 0.5f, 0.5f,
        // V4
        -0.5f, 0.5f, -0.5f,
        // V5
        0.5f, 0.5f, -0.5f,
        // V6
        -0.5f, -0.5f, -0.5f,
        // V7
        0.5f, -0.5f, -0.5f,
    ),
    floatArrayOf(
        0.5f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f,
        0.0f, 0.0f, 0.5f,
        0.0f, 0.5f, 0.5f,
        0.5f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f,
        0.0f, 0.0f, 0.5f,
        0.0f, 0.5f, 0.5f,
    ),
    intArrayOf(
        // Front face
        0, 1, 3, 3, 1, 2,
        // Top Face
        4, 0, 3, 5, 4, 3,
        // Right face
        3, 2, 7, 5, 3, 7,
        // Left face
        6, 1, 0, 6, 0, 4,
        // Bottom face
        2, 1, 6, 2, 6, 7,
        // Back face
        7, 6, 4, 7, 4, 5,
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
        val cubeModel = Model(cubeModelId, listOf(TEST_CUBE))
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
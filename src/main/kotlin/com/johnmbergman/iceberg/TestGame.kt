package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.scene.Camera
import com.johnmbergman.iceberg.scene.Entity
import com.johnmbergman.iceberg.scene.ModelLoader
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.ui.UserInterface
import imgui.ImGui
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW.*
import com.johnmbergman.iceberg.input.MouseInput


private const val MOUSE_SENSITIVITY = 0.1f
private const val MOVEMENT_SPEED = 0.025f

class TestGame: IAppLogic {

    private val displayInc = Vector4f()
    private val cubeEntities = mutableListOf<Entity>()
    private var rotation = 0.toDouble()
    private lateinit var ui: UserInterface

    fun start() {
        Engine(this).start()
    }

    override fun onInit(window: Window, scene: Scene, renderer: Renderer, ui: UserInterface) {
        this.ui = ui
        window.mouseInput.isCaptured = false
        // Tab switches between steering the camera (cursor captured) and using the UI (cursor free)
        window.addKeyListener { key, action, _ ->
            if (key == GLFW_KEY_TAB && action == GLFW_PRESS && !ui.wantsKeyboard) {
                window.mouseInput.isCaptured = !window.mouseInput.isCaptured
            }
        }

        val cubeModel = ModelLoader.loadModel("cube-model", "models/cube/cube.obj", scene.textureCache)
        scene.addModel(cubeModel)

        for (i in 0 until 10) {
            val cubeEntity = Entity("cube-entity-$i", cubeModel.id)
            cubeEntity.setPosition(i.toFloat(), 0f, -2f)
            scene.addEntity(cubeEntity)
            cubeEntities.add(cubeEntity)
        }
    }

    override fun onInput(window: Window, scene: Scene, deltaMillis: Long) {
        val move = deltaMillis * MOVEMENT_SPEED

        val camera: Camera = scene.camera
        if (!ui.wantsKeyboard) {
            if (window.isKeyPressed(GLFW_KEY_W)) {
                camera.moveForward(move)
            } else if (window.isKeyPressed(GLFW_KEY_S)) {
                camera.moveBackwards(move)
            }
            if (window.isKeyPressed(GLFW_KEY_A)) {
                camera.moveLeft(move)
            } else if (window.isKeyPressed(GLFW_KEY_D)) {
                camera.moveRight(move)
            }
            if (window.isKeyPressed(GLFW_KEY_UP)) {
                camera.moveUp(move)
            } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
                camera.moveDown(move)
            }
        }

        if (!ui.wantsMouse) {
            val mouseInput: MouseInput = window.mouseInput
            val displVec = mouseInput.displVec
            camera.addRotation(
                Math.toRadians((displVec.x * MOUSE_SENSITIVITY).toDouble()).toFloat(),
                Math.toRadians((displVec.y * MOUSE_SENSITIVITY).toDouble()).toFloat()
            )
        }

        displayInc.mul(deltaMillis / 1_000.0f)
        cubeEntities.forEach { cubeEntity ->
            val entityPosition = cubeEntity.position
            cubeEntity.setPosition(
                displayInc.x + entityPosition.x,
                displayInc.y + entityPosition.y,
                displayInc.z + entityPosition.z)
            cubeEntity.scale += displayInc.w
            cubeEntity.updateModelMatrix()
        }
    }

    override fun onDrawUI(window: Window, scene: Scene) {
        ImGui.showDemoWindow()
    }

    override fun onUpdate(window: Window, scene: Scene, deltaMillis: Long) {
        rotation += 1.5f
        if (rotation > 360) {
            rotation = 0.0
        }
        cubeEntities.forEach { cubeEntity ->
            cubeEntity.setRotation(1f, 1f, 1f, Math.toRadians(rotation).toFloat())
        }
    }
}
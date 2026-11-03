package com.johnmbergman.iceberg.sample

import com.johnmbergman.iceberg.Engine
import com.johnmbergman.iceberg.IAppLogic
import com.johnmbergman.iceberg.Window
import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.scene.Camera
import com.johnmbergman.iceberg.scene.Entity
import com.johnmbergman.iceberg.scene.ModelLoader
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.scene.SkyBox
import com.johnmbergman.iceberg.scene.lighting.DirectionalLight
import com.johnmbergman.iceberg.scene.lighting.PointLight
import com.johnmbergman.iceberg.scene.lighting.SpotLight
import com.johnmbergman.iceberg.ui.UserInterface
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import com.johnmbergman.iceberg.input.MouseInput


private const val MOUSE_SENSITIVITY = 0.1f
private const val MOVEMENT_SPEED = 0.025f

class TestGame: IAppLogic {

    private lateinit var ui: UserInterface
    private lateinit var lightControls: LightControls

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

        // The cube never moves, so its model matrix only needs computing once
        val cubeEntity = Entity("cube-entity", cubeModel.id)
        cubeEntity.setPosition(0f, 0f, -2f)
        cubeEntity.updateModelMatrix()
        scene.addEntity(cubeEntity)

        val skyBox = SkyBox("models/skybox/skybox.obj", scene.textureCache)
        skyBox.skyBoxEntity.scale = 50f
        skyBox.skyBoxEntity.updateModelMatrix()
        scene.skyBox = skyBox

        scene.environment.ambientColor.set(1f)
        scene.environment.ambientIntensity = 0.3f
        scene.addLight(DirectionalLight(direction = Vector3f(0f, 1f, 0f)))
        scene.addLight(PointLight(position = Vector3f(0f, 0f, -1.4f)))
        // Starts off (intensity 0); turn it up from the light controls
        scene.addLight(
            SpotLight(
                position = Vector3f(0f, 0f, -1.4f),
                direction = Vector3f(0f, 0f, -1f),
                cutoffAngleDegrees = 140f,
                intensity = 0f,
            )
        )
        lightControls = LightControls(scene)
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
    }

    override fun onDrawUI(window: Window, scene: Scene) {
        lightControls.draw()
    }
}

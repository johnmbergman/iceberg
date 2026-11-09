package com.johnmbergman.iceberg.sample

import com.johnmbergman.iceberg.Engine
import com.johnmbergman.iceberg.IAppLogic
import com.johnmbergman.iceberg.Window
import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.graphics.TextureCache
import com.johnmbergman.iceberg.input.MouseInput
import com.johnmbergman.iceberg.math.Color
import com.johnmbergman.iceberg.scene.*
import com.johnmbergman.iceberg.scene.lighting.DirectionalLight
import com.johnmbergman.iceberg.ui.UserInterface
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*


private const val MOUSE_SENSITIVITY = 0.1f
private const val MOVEMENT_SPEED = 0.025f
private const val LIGHT_ANGLE_STEP = 2.5f

class TestGame: IAppLogic {

    private lateinit var ui: UserInterface
    private lateinit var lightControls: LightControls
    private lateinit var dirLight: DirectionalLight

    /** Angle of the directional light from straight overhead, in degrees; -90 to 90. */
    private var lightAngle = 0f

    fun start() {
        Engine(this).start()
    }

    override fun onInit(window: Window, scene: Scene, renderer: Renderer, ui: UserInterface, textureCache: TextureCache) {
        this.ui = ui
        window.mouseInput.isCaptured = false
        // Tab switches between steering the camera (cursor captured) and using the UI (cursor free)
        window.addKeyListener { key, action, _ ->
            if (key == GLFW_KEY_TAB && action == GLFW_PRESS && !ui.wantsKeyboard) {
                window.mouseInput.isCaptured = !window.mouseInput.isCaptured
            }
        }

        // The same wall with and without a normal map, side by side, to compare the lighting
        val wallNoNormalsModel = ModelLoader.loadModel(
            "quad-no-normals-model", "models/wall/wall_nonormals.obj", textureCache
        )
        scene.addModel(wallNoNormalsModel)

        // The walls never move, so their model matrices only need computing once
        val wallLeftEntity = Entity("wall-left-entity", wallNoNormalsModel.id)
        wallLeftEntity.setPosition(-3f, 0f, 0f)
        wallLeftEntity.scale = 2f
        wallLeftEntity.updateModelMatrix()
        scene.addEntity(wallLeftEntity)

        val wallModel = ModelLoader.loadModel("quad-model", "models/wall/wall.obj", textureCache)
        scene.addModel(wallModel)

        val wallRightEntity = Entity("wall-right-entity", wallModel.id)
        wallRightEntity.setPosition(3f, 0f, 0f)
        wallRightEntity.scale = 2f
        wallRightEntity.updateModelMatrix()
        scene.addEntity(wallRightEntity)

        scene.environment.ambientColor = Color.WHITE
        scene.environment.ambientIntensity = 0.2f
        dirLight = DirectionalLight(direction = Vector3f(1f, 1f, 0f), intensity = 1f)
        scene.addLight(dirLight)
        lightAngle = -35f
        updateLightDirection()

        // Look straight down at the walls from above
        val camera = scene.camera
        camera.moveUp(5f)
        camera.addRotation(Math.toRadians(90.0).toFloat(), 0f)

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
            // Left/right swing the directional light across the sky
            if (window.isKeyPressed(GLFW_KEY_LEFT)) {
                lightAngle = (lightAngle - LIGHT_ANGLE_STEP).coerceAtLeast(-90f)
            } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
                lightAngle = (lightAngle + LIGHT_ANGLE_STEP).coerceAtMost(90f)
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

        updateLightDirection()
    }

    private fun updateLightDirection() {
        val angleRadians = Math.toRadians(lightAngle.toDouble())
        dirLight.direction.x = Math.sin(angleRadians).toFloat()
        dirLight.direction.y = Math.cos(angleRadians).toFloat()
    }

    override fun onDrawUI(window: Window, scene: Scene) {
        lightControls.draw()
    }
}

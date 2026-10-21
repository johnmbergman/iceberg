package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.Mesh
import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.scene.Scene
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

class TestGame: IAppLogic {
    fun start() {
        Engine(this).start()
    }

    override fun onInit(window: Window, scene: Scene, renderer: Renderer) {
        scene.addMesh("quad", TEST_QUAD)
    }
}
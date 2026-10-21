package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.Mesh
import com.johnmbergman.iceberg.scene.Scene
import kotlin.floatArrayOf

val TEST_QUAD = Mesh(
    floatArrayOf(
        -0.5f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.5f, 0.5f, 0.0f,
        ),
    floatArrayOf(
        0.5f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f,
        0.0f, 0.0f, 0.5f,
        0.0f, 0.5f, 0.5f,
        ),
    intArrayOf(0, 1, 3, 3, 1, 2)
)

class TestGame : IAppLogic {
    fun start() {
        val engine = Engine()
        val scene = Scene()
        scene.addMesh("quad", TEST_QUAD)
        engine.start(scene)
    }
}
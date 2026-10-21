package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.Mesh
import com.johnmbergman.iceberg.scene.Scene

// This code is written to simulate and test the game engine during development.
fun main() {
    val engine = Engine()
    val scene = Scene()
    val positions = floatArrayOf(
        -0.5f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.5f, 0.5f, 0.0f,
    )
    val colors = floatArrayOf(
        0.5f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f,
        0.0f, 0.0f, 0.5f,
        0.0f, 0.5f, 0.5f,
    )
    val indices = intArrayOf(0, 1, 3, 3, 1, 2)
    val mesh = Mesh(positions, colors, indices)
    scene.addMesh("quad", mesh)
    engine.start(scene)
}
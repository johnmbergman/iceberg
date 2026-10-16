package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.Mesh
import com.johnmbergman.iceberg.scene.Scene

// This code is written to simulate and test the game engine during development.
fun main() {
    val engine = Engine()
    val scene = Scene()
    val positions = floatArrayOf(
        0.0f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f
    )
    val mesh = Mesh(positions, 3)
    scene.addMesh("triangle", mesh)
    engine.start(scene)
}
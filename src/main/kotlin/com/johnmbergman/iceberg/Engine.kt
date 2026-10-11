package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.core.graphics.OpenGLRenderer
import com.johnmbergman.iceberg.core.graphics.Renderer

class Engine {

    private val window: Window
    private val targetFramesPerSecond: Int = 30
    private val targetUpdatesPerSecond: Int = 30
    private val renderer: Renderer

    private var running: Boolean = false

    constructor(windowTitle: String, ) {
        window = Window("Game Window")
        renderer = OpenGLRenderer()
        running = true
    }

    private fun run() {
        var initialTime = System.currentTimeMillis()
        var timeU = 1000.0f / targetUpdatesPerSecond
        var timeR = if (targetFramesPerSecond > 0) 1000.0f / targetFramesPerSecond else 0.0f
        var deltaUpdate = 0.0f
        var deltaFramesPerSecond = 0.0f
        var updateTime = initialTime
        while (running && !window.shouldClose()) {
            window.pollEvents()

            val now = System.currentTimeMillis()
            deltaUpdate += (now - initialTime) / timeU
            deltaFramesPerSecond += (now - initialTime) / timeR

            if (deltaUpdate >= 1) {
                val diffTimeMillis = now - updateTime
                updateTime = now
                deltaUpdate--
            }

            if (targetFramesPerSecond <= 0 || deltaFramesPerSecond >= 1) {
                renderer.render(window)
                deltaFramesPerSecond--
                window.update()
            }
            initialTime = now
        }
        cleanup()
    }

    private fun cleanup() {
        renderer.cleanup()
        window.cleanup()
    }

    fun start() {
        running = true
        run()
    }

    fun stop() {
        running = false
    }
}
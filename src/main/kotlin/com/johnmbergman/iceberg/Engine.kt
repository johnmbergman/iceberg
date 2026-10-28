package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.OpenGLRenderer
import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.monitoring.Logger
import com.johnmbergman.iceberg.monitoring.SimpleLogger
import com.johnmbergman.iceberg.scene.Scene

class Engine {

    private val platform: Platform
    private val window: Window
    // 0 = uncapped; vsync (Window's swap interval) then paces rendering
    private val targetFramesPerSecond: Int = 0
    private val targetUpdatesPerSecond: Int = 60
    private val renderer: Renderer
    private val logger: Logger = SimpleLogger()
    private val handler: IAppLogic

    private var currentScene: Scene
    private var running: Boolean = false

    constructor(handler: IAppLogic) {
        this.handler = handler

        logger.debug("Initializing game engine...")
        platform = Platform()
        logger.debug("Platform initialized")
        window =Window("Game Window", onResize = ::resize)
        logger.debug("Window created: ${window.windowHandle}")
        renderer = OpenGLRenderer()
        logger.debug("Renderer initialized")
        currentScene = Scene()
        resize()
        handler.onInit(window, currentScene, renderer)
        running = true
    }

    private fun run() {
        logger.debug("Starting game engine...")
        var initialTime = System.currentTimeMillis()
        var timeU = 1000.0f / targetUpdatesPerSecond
        var timeR = if (targetFramesPerSecond > 0) 1000.0f / targetFramesPerSecond else 0.0f
        var deltaUpdate = 0.0f
        var deltaFramesPerSecond = 0.0f
        var updateTime = initialTime
        var inputTime = initialTime
        while (running && !window.shouldClose()) {
            platform.pollEvents()

            val now = System.currentTimeMillis()
            deltaUpdate += (now - initialTime) / timeU
            deltaFramesPerSecond += (now - initialTime) / timeR

            if (targetFramesPerSecond <= 0 || deltaFramesPerSecond >= 1) {
                window.mouseInput.input()
                handler.onInput(window, currentScene, now - inputTime)
                inputTime = now
            }

            if (deltaUpdate >= 1) {
                val diffTimeMillis = now - updateTime
                updateTime = now
                handler.onUpdate(window, currentScene, diffTimeMillis)
                deltaUpdate--
            }

            if (targetFramesPerSecond <= 0 || deltaFramesPerSecond >= 1) {
                renderer.render(window, currentScene)
                deltaFramesPerSecond--
                window.update()
            }
            initialTime = now
        }
        cleanup()
    }

    private fun cleanup() {
        logger.debug("Cleaning up root handler...")
        handler.cleanup()
        logger.debug("Cleaning up game engine...")
        renderer.cleanup()
        logger.debug("Cleaning up scene...")
        currentScene.cleanup()
        logger.debug("Cleaning up window...")
        window.cleanup()
        logger.debug("Window cleaned up")
        logger.debug("Cleaning up platform...")
        platform.cleanup()
        logger.debug("Platform cleaned up")
    }

    private fun resize() {
        currentScene.resize(window.width, window.height)
    }

    fun start() {
        running = true
        run()
    }

    fun stop() {
        running = false
    }
}
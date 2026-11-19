package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.ResourceCache
import com.johnmbergman.iceberg.graphics.OpenGLRenderer
import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.monitoring.Logger
import com.johnmbergman.iceberg.monitoring.SimpleLogger
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.ui.UserInterface
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import kotlin.time.TimeSource

class Engine {

    private val platform: Platform
    private val window: Window
    // Monotonic, so frame deltas never go backwards or jump when the system clock is adjusted
    private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic
    private val renderer: Renderer
    private val resources: ResourceCache
    private val ui: UserInterface
    private val logger: Logger = SimpleLogger()
    private val handler: IAppLogic
    private val context: AppContext

    private var running: Boolean = false

    constructor(handler: IAppLogic) {
        this.handler = handler

        logger.debug("Initializing game engine...")
        platform = Platform()
        logger.debug("Platform initialized")
        window =Window("Game Window", onResize = ::resize)
        logger.debug("Window created: ${window.windowHandle}")
        // The UI context must exist before the renderer, which builds the UI font atlas
        ui = UserInterface(window)
        logger.debug("User interface initialized")
        // Models and textures are GPU resources, so the resource cache needs the window's OpenGL context
        resources = ResourceCache()
        logger.debug("Resource cache initialized")
        renderer = OpenGLRenderer(resources)
        logger.debug("Renderer initialized")
        context = AppContext(window, ui, resources, Scene())
        resize()
        // Escape quits unless the UI is using the keyboard, e.g., to cancel text entry. This acts on press
        // because ImGui releases the keyboard as soon as the text field closes, before the key comes back up.
        window.addKeyListener { key, action, _ ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS && !ui.wantsKeyboard) {
                window.requestClose()
            }
        }
        handler.onInit(context)
        running = true
    }

    /**
     * Runs one input, update and render per pass, each given the real time since the previous pass (a variable
     * timestep). Vsync (the window's swap interval) paces the passes.
     */
    private fun run() {
        logger.debug("Starting game engine...")
        var lastFrame = timeSource.markNow()
        while (running && !window.shouldClose()) {
            platform.pollEvents()
            val now = timeSource.markNow()
            val delta = now - lastFrame
            lastFrame = now

            window.mouseInput.input()
            ui.beginFrame(delta)
            handler.onInput(context, delta)
            handler.onUpdate(context, delta)

            handler.onDrawUI(context)
            ui.endFrame()
            renderer.render(window, context.scene)
            window.update()
        }
        cleanup()
    }

    private fun cleanup() {
        logger.debug("Cleaning up root handler...")
        handler.cleanup()
        logger.debug("Cleaning up game engine...")
        renderer.cleanup()
        logger.debug("Cleaning up user interface...")
        ui.cleanup()
        // Models and textures must be deleted while the window's OpenGL context still exists
        logger.debug("Cleaning up resources...")
        resources.cleanup()
        logger.debug("Cleaning up window...")
        window.cleanup()
        logger.debug("Window cleaned up")
        logger.debug("Cleaning up platform...")
        platform.cleanup()
        logger.debug("Platform cleaned up")
    }

    private fun resize() {
        context.scene.resize(window.width, window.height)
    }

    fun start() {
        running = true
        run()
    }

    fun stop() {
        running = false
    }
}
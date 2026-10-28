package com.johnmbergman.iceberg

import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwSetErrorCallback
import org.lwjgl.glfw.GLFW.glfwTerminate

/**
 * Owns process-wide GLFW state. Must be created before any Window and cleaned up after all of them.
 * GLFW requires these calls to happen on the main thread.
 */
class Platform {

    init {
        // Set before glfwInit so init and window-creation failures are reported
        glfwSetErrorCallback { error, description ->
            System.err.println("GLFW error $error: $description")
        }
        if (!glfwInit()) {
            glfwSetErrorCallback(null)?.free()
            throw IllegalStateException("Unable to initialize GLFW")
        }
    }

    /** Processes pending events for every window. */
    fun pollEvents() {
        glfwPollEvents()
    }

    fun cleanup() {
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
}

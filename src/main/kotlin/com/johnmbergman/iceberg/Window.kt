package com.johnmbergman.iceberg

import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR
import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR
import org.lwjgl.glfw.GLFW.GLFW_FALSE
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RELEASE
import org.lwjgl.glfw.GLFW.GLFW_RESIZABLE
import org.lwjgl.glfw.GLFW.GLFW_TRUE
import org.lwjgl.glfw.GLFW.GLFW_VISIBLE
import org.lwjgl.glfw.GLFW.glfwCreateWindow
import org.lwjgl.glfw.GLFW.glfwDefaultWindowHints
import org.lwjgl.glfw.GLFW.glfwDestroyWindow
import org.lwjgl.glfw.GLFW.glfwGetKey
import org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor
import org.lwjgl.glfw.GLFW.glfwGetVideoMode
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwSetErrorCallback
import org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback
import org.lwjgl.glfw.GLFW.glfwSetKeyCallback
import org.lwjgl.glfw.GLFW.glfwSetWindowPos
import org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose
import org.lwjgl.glfw.GLFW.glfwShowWindow
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.glfw.GLFW.glfwSwapInterval
import org.lwjgl.glfw.GLFW.glfwTerminate
import org.lwjgl.glfw.GLFW.glfwWindowHint
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import java.util.concurrent.Callable


class Window {

    val windowHandle: Long
    var width: Int = 1024
        private set
    var height: Int = 768
        private set

    private val onResize: Callable<Void>?

    constructor(title: String, initialWidth: Int = 1024, initialHeight: Int = 768, fullscreen: Boolean = false, onResize: Callable<Void>? = null) {
        this.onResize = onResize

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() ) {
            throw IllegalStateException("Unable to initialize GLFW");
        }

        // Initialize GLFW
        glfwDefaultWindowHints() // Optional, the current window hints are already default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE) // required on macOS
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // The window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // The window will be resizable

        // Set the resolution
        if (fullscreen) {
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE)
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
            if (videoMode != null) {
                this.width = videoMode.width()
                this.height = videoMode.height()
            } else {
                System.err.println("Failed to get video mode")
            }
        } else {
            this.width = initialWidth
            this.height = initialHeight
        }

        // Create the window
        windowHandle = glfwCreateWindow(width, height, "Hello World!", NULL, NULL)
        if (windowHandle == NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        // This will check for the release of an escape key, and will close the window.
        // This should be removed when proper input handling is established in the engine.
        glfwSetKeyCallback(windowHandle) { window, key, _, action, _ ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
            }
        }

        // Detect when the window is resized
        glfwSetFramebufferSizeCallback(windowHandle) { _, width, height -> resize(width, height) }

        glfwSetErrorCallback { error, description ->
            System.err.println("GLFW error $error: $description")
        }

        // Get the thread stack and push a new frame
        stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowHandle, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

            // Center the window
            glfwSetWindowPos(windowHandle,
                (vidmode?.width()?.minus(pWidth[0]))?.div(2) ?: 0,
                (vidmode?.height()?.minus(pHeight[0]))?.div(2) ?: 0
            )
        } // The stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle)

        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(windowHandle)
    }

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        try {
            onResize?.call()
        } catch (ex: Exception) {
            System.err.println("Error calling resize callback")
            ex.printStackTrace(System.err)
        }
    }

    fun cleanup() {
        glfwFreeCallbacks(windowHandle)
        glfwDestroyWindow(windowHandle)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    fun update() {
        glfwSwapBuffers(windowHandle)
    }

    // Is there a better way to do this?
    fun pollEvents() {
        glfwPollEvents()
    }

    fun isKeyPressed(keyCode: Int): Boolean {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS
    }

    fun shouldClose(): Boolean {
        return glfwWindowShouldClose(windowHandle)
    }
}


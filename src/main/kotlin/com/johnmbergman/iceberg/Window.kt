package com.johnmbergman.iceberg

import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR
import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR
import org.lwjgl.glfw.GLFW.GLFW_FALSE
import org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RESIZABLE
import org.lwjgl.glfw.GLFW.GLFW_TRUE
import org.lwjgl.glfw.GLFW.GLFW_VISIBLE
import org.lwjgl.glfw.GLFW.glfwCreateWindow
import org.lwjgl.glfw.GLFW.glfwDefaultWindowHints
import org.lwjgl.glfw.GLFW.glfwDestroyWindow
import org.lwjgl.glfw.GLFW.glfwGetFramebufferSize
import org.lwjgl.glfw.GLFW.glfwGetKey
import org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor
import org.lwjgl.glfw.GLFW.glfwGetVideoMode
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.glfw.GLFW.glfwSetCharCallback
import org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback
import org.lwjgl.glfw.GLFW.glfwSetKeyCallback
import org.lwjgl.glfw.GLFW.glfwSetWindowPos
import org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose
import org.lwjgl.glfw.GLFW.glfwShowWindow
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.glfw.GLFW.glfwSwapInterval
import org.lwjgl.glfw.GLFW.glfwWindowHint
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import com.johnmbergman.iceberg.input.MouseInput


class Window {

    val windowHandle: Long
    val mouseInput: MouseInput
    var width: Int = 1024
        private set
    var height: Int = 768
        private set

    private val onResize: (() -> Unit)?
    // GLFW allows one callback per event type, so the window owns them and fans events out to listeners
    private val keyListeners = mutableListOf<(key: Int, action: Int, mods: Int) -> Unit>()
    private val charListeners = mutableListOf<(codepoint: Int) -> Unit>()

    constructor(title: String, initialWidth: Int = 1024, initialHeight: Int = 768, fullscreen: Boolean = false, onResize: (() -> Unit)? = null) {
        this.onResize = onResize

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
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL)
        if (windowHandle == NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Key events fire on press, repeat and release; char events carry typed text after keyboard layout is applied
        glfwSetKeyCallback(windowHandle) { _, key, _, action, mods ->
            keyListeners.forEach { it(key, action, mods) }
        }
        glfwSetCharCallback(windowHandle) { _, codepoint ->
            charListeners.forEach { it(codepoint) }
        }

        // Detect when the window is resized
        glfwSetFramebufferSizeCallback(windowHandle) { _, width, height -> resize(width, height) }

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

        // Make the OpenGL context current and load its function pointers, so GL resources can be created from here on
        glfwMakeContextCurrent(windowHandle)
        GL.createCapabilities()

        // On high-DPI displays the framebuffer is larger than the window size in screen coordinates
        stackPush().use { stack ->
            val framebufferWidth = stack.mallocInt(1)
            val framebufferHeight = stack.mallocInt(1)
            glfwGetFramebufferSize(windowHandle, framebufferWidth, framebufferHeight)
            width = framebufferWidth[0]
            height = framebufferHeight[0]
        }

        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(windowHandle)

        // Initialize input system after showing the window so the initial hover state is accurate
        mouseInput = MouseInput(windowHandle)
    }

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        try {
            onResize?.invoke()
        } catch (ex: Exception) {
            System.err.println("Error calling resize callback")
            ex.printStackTrace(System.err)
        }
    }

    fun cleanup() {
        glfwFreeCallbacks(windowHandle)
        glfwDestroyWindow(windowHandle)
    }

    fun update() {
        glfwSwapBuffers(windowHandle)
    }

    /** Listeners receive GLFW key codes, actions (press, repeat, release) and modifier bits. */
    fun addKeyListener(listener: (key: Int, action: Int, mods: Int) -> Unit) {
        keyListeners.add(listener)
    }

    /** Listeners receive Unicode code points of typed characters. */
    fun addCharListener(listener: (codepoint: Int) -> Unit) {
        charListeners.add(listener)
    }

    /** Asks the game loop to exit at the start of the next iteration. */
    fun requestClose() {
        glfwSetWindowShouldClose(windowHandle, true)
    }

    fun isKeyPressed(keyCode: Int): Boolean {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS
    }

    fun shouldClose(): Boolean {
        return glfwWindowShouldClose(windowHandle)
    }
}


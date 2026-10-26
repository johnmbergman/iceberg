package org.lwjglb.engine

import org.joml.Vector2f
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWCursorPosCallbackI

class MouseInput(private val windowHandle: Long) {
    val currentPos: Vector2f = Vector2f()
    val displVec: Vector2f = Vector2f()
    var isLeftButtonPressed: Boolean
        private set
    private val previousPos: Vector2f = Vector2f()
    var isRightButtonPressed: Boolean
        private set
    private var hasPreviousPos = false

    /**
     * When captured, the cursor is hidden and locked to the window, and movement is reported
     * as unbounded virtual motion. GLFW releases the cursor while the window is unfocused.
     */
    var isCaptured: Boolean = false
        set(value) {
            field = value
            GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, if (value) GLFW.GLFW_CURSOR_DISABLED else GLFW.GLFW_CURSOR_NORMAL)
            if (GLFW.glfwRawMouseMotionSupported()) {
                GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_RAW_MOUSE_MOTION, if (value) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE)
            }
            // The cursor position jumps when the mode changes, so don't treat it as movement
            hasPreviousPos = false
        }

    init {
        this.isLeftButtonPressed = false
        this.isRightButtonPressed = false

        GLFW.glfwSetCursorPosCallback(windowHandle, GLFWCursorPosCallbackI { handle: Long, xpos: Double, ypos: Double ->
            currentPos.x = xpos.toFloat()
            currentPos.y = ypos.toFloat()
        })
        GLFW.glfwSetMouseButtonCallback(windowHandle) { _: Long, button: Int, action: Int, _: Int ->
            when (button) {
                GLFW.GLFW_MOUSE_BUTTON_LEFT -> isLeftButtonPressed = action == GLFW.GLFW_PRESS
                GLFW.GLFW_MOUSE_BUTTON_RIGHT -> isRightButtonPressed = action == GLFW.GLFW_PRESS
            }
        }
    }

    fun input() {
        displVec.x = 0f
        displVec.y = 0f
        // Query hover state each frame; the cursor enter callback only fires on transitions,
        // so it misses a cursor that is already over the window when it opens.
        // A captured cursor's virtual position is unbounded, so hover doesn't apply.
        val inWindow = isCaptured || GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_HOVERED) == GLFW.GLFW_TRUE
        if (hasPreviousPos && inWindow) {
            val deltax = (currentPos.x - previousPos.x).toDouble()
            val deltay = (currentPos.y - previousPos.y).toDouble()
            val rotateX = deltax != 0.0
            val rotateY = deltay != 0.0
            if (rotateX) {
                displVec.y = deltax.toFloat()
            }
            if (rotateY) {
                displVec.x = deltay.toFloat()
            }
        }
        previousPos.x = currentPos.x
        previousPos.y = currentPos.y
        hasPreviousPos = true
    }
}
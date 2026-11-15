package com.johnmbergman.iceberg.input

import org.joml.Vector2f
import org.lwjgl.glfw.GLFW

/** Connects GLFW's mouse callbacks to a [MouseState]. The movement math lives in [MouseState]. */
class MouseInput(private val windowHandle: Long) {
    private val state = MouseState()

    val currentPos: Vector2f get() = state.currentPos
    /** Movement since the previous [input], with the axes swapped; see [MouseState.displVec]. */
    val displVec: Vector2f get() = state.displVec
    /** Scroll wheel / trackpad offset received since the previous [input] call; positive y scrolls up. */
    val scrollDelta: Vector2f get() = state.scrollDelta
    val isLeftButtonPressed: Boolean get() = state.isLeftButtonPressed
    val isRightButtonPressed: Boolean get() = state.isRightButtonPressed
    val isMiddleButtonPressed: Boolean get() = state.isMiddleButtonPressed

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
            state.resetMotion()
        }

    init {
        GLFW.glfwSetCursorPosCallback(windowHandle) { _, xpos, ypos ->
            state.onCursorMoved(xpos.toFloat(), ypos.toFloat())
        }
        GLFW.glfwSetMouseButtonCallback(windowHandle) { _, button, action, _ ->
            val pressed = action == GLFW.GLFW_PRESS
            when (button) {
                GLFW.GLFW_MOUSE_BUTTON_LEFT -> state.isLeftButtonPressed = pressed
                GLFW.GLFW_MOUSE_BUTTON_RIGHT -> state.isRightButtonPressed = pressed
                GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> state.isMiddleButtonPressed = pressed
            }
        }
        GLFW.glfwSetScrollCallback(windowHandle) { _, xoffset, yoffset ->
            state.onScroll(xoffset.toFloat(), yoffset.toFloat())
        }
    }

    /** Call once per frame, after polling events. */
    fun input() {
        // Query hover state each frame; the cursor enter callback only fires on transitions,
        // so it misses a cursor that is already over the window when it opens.
        // A captured cursor's virtual position is unbounded, so hover doesn't apply.
        val inWindow = isCaptured || GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_HOVERED) == GLFW.GLFW_TRUE
        state.update(inWindow)
    }
}

package com.johnmbergman.iceberg.ui

import com.johnmbergman.iceberg.Window
import imgui.ImGui
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiMouseButton
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack.stackPush
import kotlin.time.Duration
import kotlin.time.DurationUnit

// ImGui requires a positive delta time; used if two frames are timed as simultaneous
private const val MINIMUM_DELTA_SECONDS = 1.0f / 1_000.0f

/**
 * Owns the ImGui context and drives its frame lifecycle and input. Contains no rendering code;
 * the renderer draws whatever [ImGui.getDrawData] holds after [endFrame].
 */
class UserInterface(private val window: Window) {

    /** True when ImGui wants mouse input, e.g., the cursor is over a UI window; game logic should ignore the mouse. */
    val wantsMouse: Boolean
        get() = ImGui.getIO().wantCaptureMouse

    /** True when ImGui wants keyboard input, e.g., a text field has focus; game logic should ignore the keyboard. */
    val wantsKeyboard: Boolean
        get() = ImGui.getIO().wantCaptureKeyboard

    init {
        ImGui.createContext()
        // Don't persist window layouts to imgui.ini in the working directory
        ImGui.getIO().iniFilename = null

        window.addKeyListener(::onKey)
        window.addCharListener { codepoint -> ImGui.getIO().addInputCharacter(codepoint) }
    }

    /** Call once per rendered frame, after [com.johnmbergman.iceberg.input.MouseInput.input] has updated the mouse state. */
    fun beginFrame(delta: Duration) {
        val io = ImGui.getIO()
        io.deltaTime = maxOf(delta.toDouble(DurationUnit.SECONDS).toFloat(), MINIMUM_DELTA_SECONDS)

        // ImGui works in screen coordinates, while window.width/height are framebuffer pixels;
        // on high-DPI displays the two differ, so pass the ratio as the framebuffer scale
        stackPush().use { stack ->
            val screenWidth = stack.mallocInt(1)
            val screenHeight = stack.mallocInt(1)
            glfwGetWindowSize(window.windowHandle, screenWidth, screenHeight)
            io.setDisplaySize(screenWidth[0].toFloat(), screenHeight[0].toFloat())
            if (screenWidth[0] > 0 && screenHeight[0] > 0) {
                io.setDisplayFramebufferScale(
                    window.width.toFloat() / screenWidth[0],
                    window.height.toFloat() / screenHeight[0]
                )
            }
        }

        val mouseInput = window.mouseInput
        if (mouseInput.isCaptured) {
            // A captured cursor is hidden and its position unbounded, so tell ImGui there is no mouse
            io.addMousePosEvent(-Float.MAX_VALUE, -Float.MAX_VALUE)
        } else {
            // The cursor position callback reports screen coordinates, matching the display size
            io.addMousePosEvent(mouseInput.currentPos.x, mouseInput.currentPos.y)
            if (mouseInput.scrollDelta.x != 0f || mouseInput.scrollDelta.y != 0f) {
                io.addMouseWheelEvent(mouseInput.scrollDelta.x, mouseInput.scrollDelta.y)
            }
        }
        io.addMouseButtonEvent(ImGuiMouseButton.Left, mouseInput.isLeftButtonPressed)
        io.addMouseButtonEvent(ImGuiMouseButton.Right, mouseInput.isRightButtonPressed)
        io.addMouseButtonEvent(ImGuiMouseButton.Middle, mouseInput.isMiddleButtonPressed)

        ImGui.newFrame()
    }

    private fun onKey(key: Int, action: Int, mods: Int) {
        // ImGui generates its own key repeats from the held state
        if (action == GLFW_REPEAT) {
            return
        }
        val io = ImGui.getIO()
        // Modifiers are reported as their own keys so shortcuts like Ctrl+C see the current state
        io.addKeyEvent(ImGuiKey.ImGuiMod_Ctrl, (mods and GLFW_MOD_CONTROL) != 0)
        io.addKeyEvent(ImGuiKey.ImGuiMod_Shift, (mods and GLFW_MOD_SHIFT) != 0)
        io.addKeyEvent(ImGuiKey.ImGuiMod_Alt, (mods and GLFW_MOD_ALT) != 0)
        io.addKeyEvent(ImGuiKey.ImGuiMod_Super, (mods and GLFW_MOD_SUPER) != 0)

        val imGuiKey = toImGuiKey(key)
        if (imGuiKey != ImGuiKey.None) {
            io.addKeyEvent(imGuiKey, action == GLFW_PRESS)
        }
    }

    fun endFrame() {
        ImGui.render()
    }

    fun cleanup() {
        ImGui.destroyContext()
    }
}

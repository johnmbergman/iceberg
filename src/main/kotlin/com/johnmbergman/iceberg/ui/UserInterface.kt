package com.johnmbergman.iceberg.ui

import com.johnmbergman.iceberg.Window
import imgui.ImGui
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiMouseButton
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack.stackPush

// ImGui requires a positive delta time; used when two frames land in the same millisecond
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
    fun beginFrame(deltaMillis: Long) {
        val io = ImGui.getIO()
        io.deltaTime = maxOf(deltaMillis / 1_000.0f, MINIMUM_DELTA_SECONDS)

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

    private fun toImGuiKey(glfwKey: Int): Int = when (glfwKey) {
        // These ranges are contiguous in both GLFW and ImGui
        in GLFW_KEY_0..GLFW_KEY_9 -> ImGuiKey._0 + (glfwKey - GLFW_KEY_0)
        in GLFW_KEY_A..GLFW_KEY_Z -> ImGuiKey.A + (glfwKey - GLFW_KEY_A)
        in GLFW_KEY_F1..GLFW_KEY_F24 -> ImGuiKey.F1 + (glfwKey - GLFW_KEY_F1)
        in GLFW_KEY_KP_0..GLFW_KEY_KP_9 -> ImGuiKey.Keypad0 + (glfwKey - GLFW_KEY_KP_0)
        GLFW_KEY_TAB -> ImGuiKey.Tab
        GLFW_KEY_LEFT -> ImGuiKey.LeftArrow
        GLFW_KEY_RIGHT -> ImGuiKey.RightArrow
        GLFW_KEY_UP -> ImGuiKey.UpArrow
        GLFW_KEY_DOWN -> ImGuiKey.DownArrow
        GLFW_KEY_PAGE_UP -> ImGuiKey.PageUp
        GLFW_KEY_PAGE_DOWN -> ImGuiKey.PageDown
        GLFW_KEY_HOME -> ImGuiKey.Home
        GLFW_KEY_END -> ImGuiKey.End
        GLFW_KEY_INSERT -> ImGuiKey.Insert
        GLFW_KEY_DELETE -> ImGuiKey.Delete
        GLFW_KEY_BACKSPACE -> ImGuiKey.Backspace
        GLFW_KEY_SPACE -> ImGuiKey.Space
        GLFW_KEY_ENTER -> ImGuiKey.Enter
        GLFW_KEY_ESCAPE -> ImGuiKey.Escape
        GLFW_KEY_LEFT_CONTROL -> ImGuiKey.LeftCtrl
        GLFW_KEY_LEFT_SHIFT -> ImGuiKey.LeftShift
        GLFW_KEY_LEFT_ALT -> ImGuiKey.LeftAlt
        GLFW_KEY_LEFT_SUPER -> ImGuiKey.LeftSuper
        GLFW_KEY_RIGHT_CONTROL -> ImGuiKey.RightCtrl
        GLFW_KEY_RIGHT_SHIFT -> ImGuiKey.RightShift
        GLFW_KEY_RIGHT_ALT -> ImGuiKey.RightAlt
        GLFW_KEY_RIGHT_SUPER -> ImGuiKey.RightSuper
        GLFW_KEY_MENU -> ImGuiKey.Menu
        GLFW_KEY_APOSTROPHE -> ImGuiKey.Apostrophe
        GLFW_KEY_COMMA -> ImGuiKey.Comma
        GLFW_KEY_MINUS -> ImGuiKey.Minus
        GLFW_KEY_PERIOD -> ImGuiKey.Period
        GLFW_KEY_SLASH -> ImGuiKey.Slash
        GLFW_KEY_SEMICOLON -> ImGuiKey.Semicolon
        GLFW_KEY_EQUAL -> ImGuiKey.Equal
        GLFW_KEY_LEFT_BRACKET -> ImGuiKey.LeftBracket
        GLFW_KEY_BACKSLASH -> ImGuiKey.Backslash
        GLFW_KEY_RIGHT_BRACKET -> ImGuiKey.RightBracket
        GLFW_KEY_GRAVE_ACCENT -> ImGuiKey.GraveAccent
        GLFW_KEY_CAPS_LOCK -> ImGuiKey.CapsLock
        GLFW_KEY_SCROLL_LOCK -> ImGuiKey.ScrollLock
        GLFW_KEY_NUM_LOCK -> ImGuiKey.NumLock
        GLFW_KEY_PRINT_SCREEN -> ImGuiKey.PrintScreen
        GLFW_KEY_PAUSE -> ImGuiKey.Pause
        GLFW_KEY_KP_DECIMAL -> ImGuiKey.KeypadDecimal
        GLFW_KEY_KP_DIVIDE -> ImGuiKey.KeypadDivide
        GLFW_KEY_KP_MULTIPLY -> ImGuiKey.KeypadMultiply
        GLFW_KEY_KP_SUBTRACT -> ImGuiKey.KeypadSubtract
        GLFW_KEY_KP_ADD -> ImGuiKey.KeypadAdd
        GLFW_KEY_KP_ENTER -> ImGuiKey.KeypadEnter
        GLFW_KEY_KP_EQUAL -> ImGuiKey.KeypadEqual
        else -> ImGuiKey.None
    }

    fun endFrame() {
        ImGui.render()
    }

    fun cleanup() {
        ImGui.destroyContext()
    }
}

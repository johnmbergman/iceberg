package com.johnmbergman.iceberg.ui

import imgui.flag.ImGuiKey
import org.lwjgl.glfw.GLFW.*

/** Maps a GLFW key code to the matching ImGui key, or [ImGuiKey.None] for keys ImGui doesn't track. */
internal fun toImGuiKey(glfwKey: Int): Int = when (glfwKey) {
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

package com.johnmbergman.iceberg.ui

import imgui.flag.ImGuiKey
import org.lwjgl.glfw.GLFW.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The digit, letter, function and keypad ranges are mapped by offset, which only works if both libraries number
 * them contiguously in the same order. Checking both ends of each range catches a gap or a reordering.
 */
class ImGuiKeysTest {

    @Test
    fun digitRangeEnds() {
        assertEquals(ImGuiKey._0, toImGuiKey(GLFW_KEY_0))
        assertEquals(ImGuiKey._9, toImGuiKey(GLFW_KEY_9))
    }

    @Test
    fun letterRangeEnds() {
        assertEquals(ImGuiKey.A, toImGuiKey(GLFW_KEY_A))
        assertEquals(ImGuiKey.Z, toImGuiKey(GLFW_KEY_Z))
    }

    @Test
    fun functionKeyRangeEnds() {
        assertEquals(ImGuiKey.F1, toImGuiKey(GLFW_KEY_F1))
        assertEquals(ImGuiKey.F12, toImGuiKey(GLFW_KEY_F12))
        assertEquals(ImGuiKey.F24, toImGuiKey(GLFW_KEY_F24))
    }

    @Test
    fun keypadRangeEnds() {
        assertEquals(ImGuiKey.Keypad0, toImGuiKey(GLFW_KEY_KP_0))
        assertEquals(ImGuiKey.Keypad9, toImGuiKey(GLFW_KEY_KP_9))
    }

    @Test
    fun individuallyMappedKeys() {
        assertEquals(ImGuiKey.Escape, toImGuiKey(GLFW_KEY_ESCAPE))
        assertEquals(ImGuiKey.LeftArrow, toImGuiKey(GLFW_KEY_LEFT))
        assertEquals(ImGuiKey.KeypadEqual, toImGuiKey(GLFW_KEY_KP_EQUAL))
    }

    @Test
    fun keysImGuiDoesNotTrackMapToNone() {
        assertEquals(ImGuiKey.None, toImGuiKey(GLFW_KEY_WORLD_1))
        assertEquals(ImGuiKey.None, toImGuiKey(GLFW_KEY_UNKNOWN))
    }
}

package com.johnmbergman.iceberg.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ColorTest {

    @Test
    fun alphaDefaultsToOpaque() {
        assertEquals(1f, Color(0.2f, 0.4f, 0.6f).a)
    }

    @Test
    fun grayConstructorSetsEveryChannel() {
        assertEquals(Color(0.5f, 0.5f, 0.5f, 1f), Color(0.5f))
    }

    @Test
    fun colorsWithTheSameComponentsAreEqual() {
        assertEquals(Color(0.1f, 0.2f, 0.3f, 0.4f), Color(0.1f, 0.2f, 0.3f, 0.4f))
        assertNotEquals(Color(0.1f, 0.2f, 0.3f, 0.4f), Color(0.1f, 0.2f, 0.3f, 1f))
    }

    @Test
    fun namedConstantsHaveExpectedComponents() {
        assertEquals(Color(0f, 0f, 0f, 1f), Color.BLACK)
        assertEquals(Color(1f, 1f, 1f, 1f), Color.WHITE)
        assertEquals(Color(1f, 0f, 0f, 1f), Color.RED)
        assertEquals(Color(0f, 1f, 0f, 1f), Color.GREEN)
        assertEquals(Color(0f, 0f, 1f, 1f), Color.BLUE)
    }

    @Test
    fun copyChangesOnlyTheGivenChannels() {
        val original = Color(0.1f, 0.2f, 0.3f, 0.4f)
        val copy = original.copy(g = 0.9f)
        assertEquals(Color(0.1f, 0.9f, 0.3f, 0.4f), copy)
        assertEquals(Color(0.1f, 0.2f, 0.3f, 0.4f), original)
    }
}

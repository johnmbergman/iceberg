package com.johnmbergman.iceberg.input

import kotlin.test.Test
import kotlin.test.assertEquals

class MouseStateTest {

    private fun assertDisplacement(x: Float, y: Float, state: MouseState) {
        assertEquals(x, state.displVec.x, "displVec.x")
        assertEquals(y, state.displVec.y, "displVec.y")
    }

    @Test
    fun firstUpdateReportsNoMovement() {
        val state = MouseState()
        state.onCursorMoved(100f, 200f)
        state.update(inWindow = true)
        assertDisplacement(0f, 0f, state)
    }

    @Test
    fun movementSwapsAxesForPitchAndYaw() {
        val state = MouseState()
        state.onCursorMoved(100f, 200f)
        state.update(inWindow = true)

        state.onCursorMoved(103f, 195f) // 3 right, 5 up
        state.update(inWindow = true)

        // x holds the vertical movement (pitch), y the horizontal (yaw)
        assertDisplacement(-5f, 3f, state)
    }

    @Test
    fun movementIsMeasuredFromThePreviousUpdateOnly() {
        val state = MouseState()
        state.update(inWindow = true)
        state.onCursorMoved(10f, 0f)
        state.update(inWindow = true)

        state.update(inWindow = true) // No new movement
        assertDisplacement(0f, 0f, state)
    }

    @Test
    fun resettingMotionDropsTheNextJump() {
        val state = MouseState()
        state.update(inWindow = true)

        state.resetMotion() // The cursor is captured or released
        state.onCursorMoved(500f, 500f)
        state.update(inWindow = true)
        assertDisplacement(0f, 0f, state)

        // Movement after that counts again
        state.onCursorMoved(501f, 500f)
        state.update(inWindow = true)
        assertDisplacement(0f, 1f, state)
    }

    @Test
    fun movementOutsideTheWindowIsIgnoredButStillTracked() {
        val state = MouseState()
        state.update(inWindow = true)

        state.onCursorMoved(50f, 0f)
        state.update(inWindow = false)
        assertDisplacement(0f, 0f, state)

        // Back inside: only the movement since the last update counts, not the trip outside
        state.onCursorMoved(52f, 0f)
        state.update(inWindow = true)
        assertDisplacement(0f, 2f, state)
    }

    @Test
    fun scrollEventsAccumulateUntilTheNextUpdate() {
        val state = MouseState()
        state.onScroll(0f, 1f)
        state.onScroll(0.5f, 2f)
        state.update(inWindow = true)
        assertEquals(0.5f, state.scrollDelta.x)
        assertEquals(3f, state.scrollDelta.y)

        state.update(inWindow = true)
        assertEquals(0f, state.scrollDelta.x)
        assertEquals(0f, state.scrollDelta.y)
    }

    @Test
    fun scrollCountsEvenOutsideTheWindow() {
        val state = MouseState()
        state.onScroll(0f, 1f)
        state.update(inWindow = false)
        assertEquals(1f, state.scrollDelta.y)
    }
}

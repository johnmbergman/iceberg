package com.johnmbergman.iceberg.input

import org.joml.Vector2f

/**
 * Mouse state and the per-frame movement math, with no GLFW calls so it can be tested without a window.
 * [MouseInput] feeds it events from GLFW callbacks and calls [update] once per frame.
 */
class MouseState {
    /** Latest cursor position, in screen coordinates. */
    val currentPos: Vector2f = Vector2f()

    /**
     * Cursor movement since the previous [update], with the axes swapped: x holds the *vertical* movement and
     * y the horizontal, so they can be passed straight to a camera as pitch and yaw.
     */
    val displVec: Vector2f = Vector2f()

    /** Scroll wheel / trackpad offset received since the previous [update]; positive y scrolls up. */
    val scrollDelta: Vector2f = Vector2f()

    var isLeftButtonPressed = false
    var isRightButtonPressed = false
    var isMiddleButtonPressed = false

    private val previousPos: Vector2f = Vector2f()
    private var hasPreviousPos = false
    // Several scroll events can arrive per frame, so they are summed until the next update
    private val pendingScroll: Vector2f = Vector2f()

    fun onCursorMoved(x: Float, y: Float) {
        currentPos.set(x, y)
    }

    fun onScroll(xOffset: Float, yOffset: Float) {
        pendingScroll.add(xOffset, yOffset)
    }

    /** Forgets the previous position, so the next [update] reports no movement. Use when the cursor jumps. */
    fun resetMotion() {
        hasPreviousPos = false
    }

    /**
     * Computes this frame's [displVec] and [scrollDelta]. Movement only counts when the cursor is [inWindow]
     * and there was a previous position to measure from, so the first frame never reports a jump.
     */
    fun update(inWindow: Boolean) {
        displVec.zero()
        scrollDelta.set(pendingScroll)
        pendingScroll.zero()
        if (hasPreviousPos && inWindow) {
            displVec.x = currentPos.y - previousPos.y
            displVec.y = currentPos.x - previousPos.x
        }
        previousPos.set(currentPos)
        hasPreviousPos = true
    }
}

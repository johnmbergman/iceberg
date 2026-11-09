package com.johnmbergman.iceberg.scene

import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * Exponential-squared fog applied by the scene shader: visibility falls off as 1 / e^((distance × density)²).
 *
 * Not a data class: copy() would share the mutable color vector between the original and the copy.
 */
class Fog(
    var active: Boolean = false,
    color: Vector3fc = Vector3f(),
    var density: Float = 0.0f,
) {
    // Each fog owns its color; the public property is a read-only view of it
    private val colorValue = Vector3f(color)

    /**
     * Assigning copies the components, so later changes to the assigned vector don't affect
     * this fog. Reading returns a live view; copy it if you need a snapshot.
     */
    var color: Vector3fc
        get() = colorValue
        set(value) { colorValue.set(value) }
}

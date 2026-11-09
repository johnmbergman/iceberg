package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.math.Color

/**
 * Exponential-squared fog applied by the scene shader: visibility falls off as 1 / e^((distance × density)²).
 */
data class Fog(
    var active: Boolean = false,
    var color: Color = Color.BLACK,
    var density: Float = 0.0f,
)

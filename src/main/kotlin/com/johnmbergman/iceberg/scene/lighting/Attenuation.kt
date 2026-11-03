package com.johnmbergman.iceberg.scene.lighting

/**
 * Represents the attenuation factors for a point light.
 * The attenuation factors determine how the intensity of
 * a point light decreases with distance from the light source.
 */
data class Attenuation(
    var constant: Float,
    var linear: Float,
    var exponent: Float
)
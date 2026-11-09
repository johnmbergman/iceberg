package com.johnmbergman.iceberg.scene.lighting

import com.johnmbergman.iceberg.math.Color
import org.joml.Vector3f
import kotlin.math.cos

class SpotLight(
    override val position: Vector3f,
    val direction: Vector3f,
    cutoffAngleDegrees: Float,
    override var color: Color = Color.WHITE,
    override var intensity: Float = 1f,
    override val attenuation: Attenuation = Attenuation(0f, 0f, 1f)
): PositionalLight {
    var cutoffAngleDegrees: Float = cutoffAngleDegrees
        set(value) {
            field = value
            cutoffCos = cos(Math.toRadians(value.toDouble())).toFloat()
        }
    var cutoffCos: Float = (cos(Math.toRadians(cutoffAngleDegrees.toDouble()))).toFloat()
        private set
}

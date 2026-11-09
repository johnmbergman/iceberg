package com.johnmbergman.iceberg.scene.lighting

import com.johnmbergman.iceberg.math.Color
import org.joml.Vector3f

class PointLight(
    override val position: Vector3f,
    override var color: Color = Color.WHITE,
    override var intensity: Float = 1f,
    override val attenuation: Attenuation = Attenuation(0f, 0f, 1f)
): PositionalLight

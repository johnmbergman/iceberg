package com.johnmbergman.iceberg.scene.lighting

import org.joml.Vector3f

class PointLight(
    override val position: Vector3f,
    override val color: Vector3f = Vector3f(1f),
    override var intensity: Float = 1f,
    override val attenuation: Attenuation = Attenuation(0f, 0f, 1f)
): PositionalLight

package com.johnmbergman.iceberg.scene.lighting

import org.joml.Vector3f

class DirectionalLight(
    val direction: Vector3f,
    override val color: Vector3f = Vector3f(1f),
    override var intensity: Float = 1f,
) : Light

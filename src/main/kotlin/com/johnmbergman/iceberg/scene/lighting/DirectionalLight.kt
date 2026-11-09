package com.johnmbergman.iceberg.scene.lighting

import com.johnmbergman.iceberg.math.Color
import org.joml.Vector3f

class DirectionalLight(
    val direction: Vector3f,
    override var color: Color = Color.WHITE,
    override var intensity: Float = 1f,
) : Light

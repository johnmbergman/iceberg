package com.johnmbergman.iceberg.scene.lighting

import org.joml.Vector3f

sealed interface Light {
    val color: Vector3f
    var intensity: Float
}
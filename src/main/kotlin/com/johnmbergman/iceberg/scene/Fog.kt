package com.johnmbergman.iceberg.scene

import org.joml.Vector3f

data class Fog(
    var active: Boolean = false,
    var color: Vector3f = Vector3f(),
    var density: Float = 0.0f,
)
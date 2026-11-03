package com.johnmbergman.iceberg.scene.lighting

import org.joml.Vector3f

sealed interface PositionalLight : Light {
    val position: Vector3f
    val attenuation: Attenuation
}
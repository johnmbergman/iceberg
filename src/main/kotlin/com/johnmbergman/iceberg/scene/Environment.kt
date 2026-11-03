package com.johnmbergman.iceberg.scene

import org.joml.Vector3f

class Environment(
    val ambientColor: Vector3f = Vector3f(0.3f),
    var ambientIntensity: Float = 1f
    // later: fog, skybox, exposure...
)
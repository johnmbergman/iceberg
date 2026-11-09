package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.math.Color

class Environment(
    var ambientColor: Color = Color(0.3f),
    var ambientIntensity: Float = 1f
    // later: fog, skybox, exposure...
)
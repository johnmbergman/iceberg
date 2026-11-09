package com.johnmbergman.iceberg.scene.lighting

import com.johnmbergman.iceberg.math.Color

sealed interface Light {
    var color: Color
    var intensity: Float
}
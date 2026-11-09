package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.math.Color

/** Scene-wide settings that don't belong to any one node. */
class Environment(
    var ambientColor: Color = Color(0.3f),
    var ambientIntensity: Float = 1f,
    var fog: Fog = Fog(),
    /** Drawn behind everything and centered on the camera, so it never gets closer; null for none. */
    var skyBoxModelId: ModelId? = null,
)

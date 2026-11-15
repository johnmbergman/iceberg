package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.monitoring.Logger
import com.johnmbergman.iceberg.scene.lighting.DirectionalLight
import com.johnmbergman.iceberg.scene.lighting.Light
import com.johnmbergman.iceberg.scene.lighting.PointLight
import com.johnmbergman.iceberg.scene.lighting.SpotLight
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc

// Must match the array sizes in scene.frag
internal const val MAX_POINT_LIGHTS = 5
internal const val MAX_SPOT_LIGHTS = 5

/** A point light chosen for a shader slot, with its position converted to view space. */
class PackedPointLight internal constructor() {
    lateinit var light: PointLight
        private set
    val viewPosition: Vector3fc field = Vector3f()

    internal fun fill(light: PointLight, viewMatrix: Matrix4fc) {
        this.light = light
        viewMatrix.transformPosition(light.position, viewPosition)
    }
}

/** A spot light chosen for a shader slot, with its position and direction converted to view space. */
class PackedSpotLight internal constructor() {
    lateinit var light: SpotLight
        private set
    val viewPosition: Vector3fc field = Vector3f()
    val viewDirection: Vector3fc field = Vector3f()

    internal fun fill(light: SpotLight, viewMatrix: Matrix4fc) {
        this.light = light
        viewMatrix.transformPosition(light.position, viewPosition)
        viewMatrix.transformDirection(light.direction, viewDirection)
    }
}

/**
 * The lights the scene shader will use this frame. Filled by [LightPacker.pack] and reused between frames, so
 * read it before the next call.
 */
class PackedLights internal constructor(maxPointLights: Int, maxSpotLights: Int) {
    /** The first directional light in the scene, or null; the shader supports only one. */
    var directional: DirectionalLight? = null
        private set
    /** [directional]'s direction in view space; meaningless when [directional] is null. */
    val directionalViewDirection: Vector3fc field = Vector3f()

    internal fun setDirectional(light: DirectionalLight, viewMatrix: Matrix4fc) {
        directional = light
        viewMatrix.transformDirection(light.direction, directionalViewDirection)
    }

    internal fun clear() {
        directional = null
        pointLightCount = 0
        spotLightCount = 0
    }

    internal val pointSlots = List(maxPointLights) { PackedPointLight() }
    internal val spotSlots = List(maxSpotLights) { PackedSpotLight() }

    var pointLightCount = 0
        internal set
    var spotLightCount = 0
        internal set

    /** The point light in shader slot [index], or null if the slot is empty. */
    fun pointLight(index: Int): PackedPointLight? = if (index < pointLightCount) pointSlots[index] else null

    /** The spot light in shader slot [index], or null if the slot is empty. */
    fun spotLight(index: Int): PackedSpotLight? = if (index < spotLightCount) spotSlots[index] else null
}

/**
 * Chooses which of the scene's lights fit in the scene shader's fixed slots and converts them to view space.
 * Lights are taken in scene order: the first directional light, the first [maxPointLights] point lights and the
 * first [maxSpotLights] spot lights. Makes no OpenGL calls and doesn't allocate per frame.
 *
 * Lights that don't fit are left out, with a warning logged whenever the number left out changes, rather than on
 * every frame.
 */
class LightPacker(
    private val logger: Logger,
    private val maxPointLights: Int = MAX_POINT_LIGHTS,
    private val maxSpotLights: Int = MAX_SPOT_LIGHTS,
) {
    private val packed = PackedLights(maxPointLights, maxSpotLights)

    private var lastDroppedDirectional = 0
    private var lastDroppedPoint = 0
    private var lastDroppedSpot = 0

    fun pack(lights: List<Light>, viewMatrix: Matrix4fc): PackedLights {
        packed.clear()
        var directionalTotal = 0
        var pointTotal = 0
        var spotTotal = 0

        // Indexed so packing doesn't create an iterator every frame
        for (i in lights.indices) {
            when (val light = lights[i]) {
                is DirectionalLight -> {
                    directionalTotal++
                    if (packed.directional == null) {
                        packed.setDirectional(light, viewMatrix)
                    }
                }
                is PointLight -> {
                    pointTotal++
                    if (packed.pointLightCount < maxPointLights) {
                        packed.pointSlots[packed.pointLightCount++].fill(light, viewMatrix)
                    }
                }
                is SpotLight -> {
                    spotTotal++
                    if (packed.spotLightCount < maxSpotLights) {
                        packed.spotSlots[packed.spotLightCount++].fill(light, viewMatrix)
                    }
                }
            }
        }

        warnIfDroppedChanged(
            directional = maxOf(directionalTotal - 1, 0),
            point = pointTotal - packed.pointLightCount,
            spot = spotTotal - packed.spotLightCount,
        )
        return packed
    }

    private fun warnIfDroppedChanged(directional: Int, point: Int, spot: Int) {
        if (directional == lastDroppedDirectional && point == lastDroppedPoint && spot == lastDroppedSpot) {
            return
        }
        lastDroppedDirectional = directional
        lastDroppedPoint = point
        lastDroppedSpot = spot

        val dropped = buildList {
            if (directional > 0) add("$directional directional (only 1 supported)")
            if (point > 0) add("$point point (only $maxPointLights supported)")
            if (spot > 0) add("$spot spot (only $maxSpotLights supported)")
        }
        if (dropped.isNotEmpty()) {
            logger.warn("Too many lights; not drawing ${dropped.joinToString()}")
        }
    }
}

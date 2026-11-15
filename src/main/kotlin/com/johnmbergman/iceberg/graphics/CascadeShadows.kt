package com.johnmbergman.iceberg.graphics

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow

// Must match NUM_CASCADES in scene.frag
internal const val SHADOW_MAP_CASCADE_COUNT = 3

// Blend between logarithmic (1) and uniform (0) split spacing. Logarithmic spacing gives the slices near the camera,
// where shadow detail is most visible, far more of the shadow map.
private const val CASCADE_SPLIT_LAMBDA = 0.95f

// The up axis for the light's view matrix is swapped from Y to Z when the light is within this cosine of vertical,
// since lookAt can't build a basis when the view direction is parallel to the up axis
private const val VERTICAL_LIGHT_COS = 0.999f

/** One slice of the camera frustum and the light-space transform that renders its shadow map. */
class CascadeShadow internal constructor() {
    internal val matrix = Matrix4f()

    /** Transforms world-space positions into this cascade's shadow map clip space. */
    val projectionViewMatrix: Matrix4fc get() = matrix

    /**
     * The view-space z where this cascade ends. Negative, since the camera looks down -z; the shader uses the
     * first cascade whose split distance a fragment hasn't passed.
     */
    var splitDistance = 0f
        internal set
}

/**
 * Splits the camera frustum into [SHADOW_MAP_CASCADE_COUNT] depth slices (cascaded shadow maps) and computes an
 * orthographic light projection for each, sized to enclose its slice. Slices near the camera are short, so their
 * shadow maps cover a small area in fine detail; distant slices cover much more ground at lower detail.
 *
 * Makes no OpenGL calls and doesn't allocate per frame.
 *
 * Derived from Sascha Willems' Vulkan examples (MIT License,
 * https://github.com/SaschaWillems/Vulkan/tree/master/examples/shadowmappingcascade), which build on
 * https://johanmedestrom.wordpress.com/2016/03/18/opengl-cascaded-shadow-maps/. The split scheme is from
 * GPU Gems 3, chapter 10.
 */
class CascadeShadows {
    val cascades: List<CascadeShadow> = List(SHADOW_MAP_CASCADE_COUNT) { CascadeShadow() }

    // Split positions as fractions of the near-to-far range, reused each update
    private val splits = FloatArray(SHADOW_MAP_CASCADE_COUNT)

    private val inverseCamera = Matrix4f()
    private val lightView = Matrix4f()
    private val corners = Array(8) { Vector3f() }
    private val sliceCorners = Array(8) { Vector3f() }
    private val center = Vector3f()
    private val toLight = Vector3f()
    private val eye = Vector3f()
    private val up = Vector3f()
    private val scratch = Vector3f()

    /**
     * Recomputes every cascade for the camera's [viewMatrix] and perspective [projectionMatrix] (whose near and
     * far planes are [near] and [far]), lit by a directional light whose [lightDirection] points from the scene
     * toward the light, in world space.
     */
    fun update(viewMatrix: Matrix4fc, projectionMatrix: Matrix4fc, near: Float, far: Float, lightDirection: Vector3fc) {
        computeSplits(near, far)
        computeFrustumCorners(viewMatrix, projectionMatrix)

        lightDirection.normalize(toLight)
        // Any perpendicular-enough axis works as up; the light view's roll doesn't matter for a square shadow map
        if (abs(toLight.y) > VERTICAL_LIGHT_COS) up.set(0f, 0f, 1f) else up.set(0f, 1f, 0f)

        val clipRange = far - near
        var lastSplit = 0f
        for (i in 0..<SHADOW_MAP_CASCADE_COUNT) {
            val split = splits[i]
            updateCascade(cascades[i], lastSplit, split)
            cascades[i].splitDistance = -(near + split * clipRange)
            lastSplit = split
        }
    }

    /** Fills [splits] with each cascade's far end as a fraction (0 to 1) of the camera's depth range. */
    private fun computeSplits(near: Float, far: Float) {
        val range = far - near
        val ratio = far / near
        for (i in 0..<SHADOW_MAP_CASCADE_COUNT) {
            val p = (i + 1) / SHADOW_MAP_CASCADE_COUNT.toFloat()
            val logarithmic = near * ratio.pow(p)
            val uniform = near + range * p
            val distance = CASCADE_SPLIT_LAMBDA * (logarithmic - uniform) + uniform
            splits[i] = (distance - near) / range
        }
    }

    /** Fills [corners] with the camera frustum's corners in world space: 0-3 on the near plane, 4-7 on the far. */
    private fun computeFrustumCorners(viewMatrix: Matrix4fc, projectionMatrix: Matrix4fc) {
        projectionMatrix.mul(viewMatrix, inverseCamera).invert()
        for (j in 0..<8) {
            val x = if (j == 1 || j == 2 || j == 5 || j == 6) 1f else -1f
            val y = if (j < 2 || j == 4 || j == 5) 1f else -1f
            val z = if (j < 4) -1f else 1f
            inverseCamera.transformProject(x, y, z, corners[j])
        }
    }

    /** Fits [cascade]'s light projection around the frustum slice between fractions [start] and [end]. */
    private fun updateCascade(cascade: CascadeShadow, start: Float, end: Float) {
        for (j in 0..<4) {
            val near = corners[j]
            corners[j + 4].sub(near, scratch)
            near.fma(start, scratch, sliceCorners[j])
            near.fma(end, scratch, sliceCorners[j + 4])
        }

        center.zero()
        sliceCorners.forEach { center.add(it) }
        center.div(8f)

        // A bounding sphere rather than a tight box, so the projection's size doesn't change as the camera turns
        var radius = 0f
        sliceCorners.forEach { radius = max(radius, it.distance(center)) }
        radius = ceil(radius * 16f) / 16f

        // Back the light's eye off along the light direction so the whole sphere is in front of it
        center.fma(radius, toLight, eye)
        lightView.setLookAt(eye, center, up)
        // zZeroToOne = true pairs with the shader's `* 0.5 + 0.5`; change both together
        cascade.matrix
            .setOrtho(-radius, radius, -radius, radius, 0f, 2f * radius, true)
            .mul(lightView)
    }
}

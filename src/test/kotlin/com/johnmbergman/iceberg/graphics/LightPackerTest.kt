package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.assertVectorEquals
import com.johnmbergman.iceberg.monitoring.Logger
import com.johnmbergman.iceberg.scene.lighting.DirectionalLight
import com.johnmbergman.iceberg.scene.lighting.Light
import com.johnmbergman.iceberg.scene.lighting.PointLight
import com.johnmbergman.iceberg.scene.lighting.SpotLight
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** LightPacker makes no OpenGL calls, so it can be tested without a context. */
class LightPackerTest {

    private class RecordingLogger : Logger {
        val warnings = mutableListOf<String>()
        override fun debug(message: String) {}
        override fun info(message: String) {}
        override fun warn(message: String) { warnings += message }
        override fun error(message: String) {}
    }

    private val logger = RecordingLogger()
    private val identity = Matrix4f()

    private fun point(x: Float = 0f) = PointLight(position = Vector3f(x, 0f, 0f))
    private fun spot(x: Float = 0f) = SpotLight(position = Vector3f(x, 0f, 0f), direction = Vector3f(0f, 0f, -1f), cutoffAngleDegrees = 30f)
    private fun directional() = DirectionalLight(direction = Vector3f(0f, -1f, 0f))

    @Test
    fun noLightsLeavesEverySlotEmpty() {
        val packed = LightPacker(logger).pack(emptyList(), identity)

        assertNull(packed.directional)
        assertEquals(0, packed.pointLightCount)
        assertEquals(0, packed.spotLightCount)
        assertNull(packed.pointLight(0))
        assertNull(packed.spotLight(0))
        assertTrue(logger.warnings.isEmpty())
    }

    @Test
    fun sortsLightsIntoTheirSlotsInSceneOrder() {
        val firstPoint = point(1f)
        val spot = spot()
        val secondPoint = point(2f)
        val sun = directional()

        val packed = LightPacker(logger).pack(listOf(firstPoint, spot, sun, secondPoint), identity)

        assertSame(sun, packed.directional)
        assertEquals(2, packed.pointLightCount)
        assertSame(firstPoint, packed.pointLight(0)?.light)
        assertSame(secondPoint, packed.pointLight(1)?.light)
        assertNull(packed.pointLight(2))
        assertEquals(1, packed.spotLightCount)
        assertSame(spot, packed.spotLight(0)?.light)
    }

    @Test
    fun spotLightsDoNotTakePointLightSlots() {
        val packed = LightPacker(logger).pack(listOf(spot(), spot()), identity)

        assertEquals(0, packed.pointLightCount)
        assertEquals(2, packed.spotLightCount)
    }

    @Test
    fun convertsPositionsAndDirectionsToViewSpace() {
        // A camera moved to x = 10 sees the world shifted by -10; directions ignore the translation
        val view = Matrix4f().translation(-10f, 0f, 0f)
        val sun = directional()
        val spot = spot(12f)

        val packed = LightPacker(logger).pack(listOf(point(11f), spot, sun), view)

        assertVectorEquals(1f, 0f, 0f, packed.pointLight(0)!!.viewPosition)
        assertVectorEquals(2f, 0f, 0f, packed.spotLight(0)!!.viewPosition)
        assertVectorEquals(0f, 0f, -1f, packed.spotLight(0)!!.viewDirection)
        assertVectorEquals(0f, -1f, 0f, packed.directionalViewDirection)
        // The scene's own vectors are untouched
        assertVectorEquals(12f, 0f, 0f, spot.position)
    }

    @Test
    fun usesOnlyTheFirstDirectionalLight() {
        val first = directional()
        val packed = LightPacker(logger).pack(listOf(first, directional()), identity)

        assertSame(first, packed.directional)
        assertEquals(1, logger.warnings.size)
        assertTrue("1 directional" in logger.warnings.single(), logger.warnings.single())
    }

    @Test
    fun keepsTheFirstLightsThatFitAndWarnsAboutTheRest() {
        val lights: List<Light> = List(MAX_POINT_LIGHTS + 2) { point(it.toFloat()) } + List(MAX_SPOT_LIGHTS + 1) { spot() }

        val packed = LightPacker(logger).pack(lights, identity)

        assertEquals(MAX_POINT_LIGHTS, packed.pointLightCount)
        assertSame<Light?>(lights[MAX_POINT_LIGHTS - 1], packed.pointLight(MAX_POINT_LIGHTS - 1)?.light)
        assertEquals(MAX_SPOT_LIGHTS, packed.spotLightCount)
        val warning = logger.warnings.single()
        assertTrue("2 point" in warning, warning)
        assertTrue("1 spot" in warning, warning)
    }

    @Test
    fun warnsOnlyWhenTheNumberLeftOutChanges() {
        val packer = LightPacker(logger, maxPointLights = 1, maxSpotLights = 1)
        val lights = mutableListOf<Light>(point(), point())

        packer.pack(lights, identity)
        packer.pack(lights, identity) // Same overflow on the next frame
        assertEquals(1, logger.warnings.size)

        lights += point()
        packer.pack(lights, identity) // One more left out
        assertEquals(2, logger.warnings.size)

        lights.subList(1, lights.size).clear()
        packer.pack(lights, identity) // Everything fits again: nothing to warn about
        assertEquals(2, logger.warnings.size)

        lights += point()
        packer.pack(lights, identity) // Overflowing again warns again
        assertEquals(3, logger.warnings.size)
    }

    @Test
    fun repackingReplacesThePreviousFrame() {
        val packer = LightPacker(logger)
        val first = point()
        packer.pack(listOf(first, directional()), identity)

        val packed = packer.pack(listOf(spot()), identity)

        assertNull(packed.directional)
        assertEquals(0, packed.pointLightCount)
        assertNull(packed.pointLight(0))
        assertEquals(1, packed.spotLightCount)
    }
}

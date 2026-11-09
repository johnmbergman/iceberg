package com.johnmbergman.iceberg.scene.lighting

import com.johnmbergman.iceberg.TOLERANCE
import org.joml.Vector3f
import kotlin.test.Test
import kotlin.test.assertEquals

class SpotLightTest {

    private fun spotLight(cutoffAngleDegrees: Float) =
        SpotLight(position = Vector3f(), direction = Vector3f(0f, 0f, -1f), cutoffAngleDegrees = cutoffAngleDegrees)

    @Test
    fun constructorComputesCutoffCosine() {
        assertEquals(0.5f, spotLight(60f).cutoffCos, TOLERANCE)
    }

    @Test
    fun changingCutoffAngleUpdatesCutoffCosine() {
        val light = spotLight(60f)

        light.cutoffAngleDegrees = 0f
        assertEquals(1f, light.cutoffCos, TOLERANCE)

        light.cutoffAngleDegrees = 90f
        assertEquals(0f, light.cutoffCos, TOLERANCE)

        light.cutoffAngleDegrees = 180f
        assertEquals(-1f, light.cutoffCos, TOLERANCE)
    }

    @Test
    fun cutoffAngleKeepsTheValueItWasGiven() {
        val light = spotLight(60f)
        light.cutoffAngleDegrees = 45f
        assertEquals(45f, light.cutoffAngleDegrees)
    }
}

package com.johnmbergman.iceberg.sample

import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.scene.lighting.DirectionalLight
import com.johnmbergman.iceberg.scene.lighting.PointLight
import com.johnmbergman.iceberg.scene.lighting.SpotLight
import imgui.ImGui
import imgui.flag.ImGuiCond
import org.joml.Vector3f

private const val FORMAT = "%.2f"

/**
 * An ImGui window for tweaking the scene's ambient light and the first directional, point and spot light.
 * Widgets edit the lights directly, so changes made elsewhere (e.g., by game logic) show up here too.
 * Call [draw] from [com.johnmbergman.iceberg.IAppLogic.onDrawUI]; the engine owns the ImGui frame.
 */
class LightControls(private val scene: Scene) {

    // ImGui widgets edit values through arrays; these are reused for every widget each frame
    private val floatValue = FloatArray(1)
    private val colorValue = FloatArray(3)

    fun draw() {
        ImGui.setNextWindowPos(0f, 0f, ImGuiCond.Always)
        ImGui.setNextWindowSize(450f, 400f)

        ImGui.begin("Lights controls")

        if (ImGui.collapsingHeader("Ambient Light")) {
            val environment = scene.environment
            sliderFloat("Ambient factor", environment.ambientIntensity, 0f, 1f) { environment.ambientIntensity = it }
            colorEdit("Ambient color", environment.ambientColor)
        }

        if (ImGui.collapsingHeader("Point Light")) {
            val light = firstLight<PointLight>()
            if (light == null) {
                ImGui.text("No point light in the scene")
            } else {
                vectorSliders("Point Light", light.position, -10f, 10f)
                colorEdit("Point Light color", light.color)
                sliderFloat("Point Light Intensity", light.intensity, 0f, 1f) { light.intensity = it }
            }
        }

        if (ImGui.collapsingHeader("Spot Light")) {
            val light = firstLight<SpotLight>()
            if (light == null) {
                ImGui.text("No spot light in the scene")
            } else {
                vectorSliders("Spot Light", light.position, -10f, 10f)
                colorEdit("Spot Light color", light.color)
                sliderFloat("Spot Light Intensity", light.intensity, 0f, 1f) { light.intensity = it }
                ImGui.separator()
                // The angle is measured from the cone's center, so 180° already lights everything
                sliderFloat("Spot Light cutoff", light.cutoffAngleDegrees, 0f, 180f, "%.0f") {
                    light.cutoffAngleDegrees = it
                }
                vectorSliders("Dir cone", light.direction, -1f, 1f)
            }
        }

        if (ImGui.collapsingHeader("Dir Light")) {
            val light = firstLight<DirectionalLight>()
            if (light == null) {
                ImGui.text("No directional light in the scene")
            } else {
                vectorSliders("Dir Light", light.direction, -1f, 1f)
                colorEdit("Dir Light color", light.color)
                sliderFloat("Dir Light Intensity", light.intensity, 0f, 1f) { light.intensity = it }
            }
        }

        ImGui.end()
    }

    private inline fun <reified T> firstLight(): T? = scene.lights.firstNotNullOfOrNull { it as? T }

    private inline fun sliderFloat(
        label: String,
        value: Float,
        min: Float,
        max: Float,
        format: String = FORMAT,
        onChange: (Float) -> Unit,
    ) {
        floatValue[0] = value
        if (ImGui.sliderFloat(label, floatValue, min, max, format)) {
            onChange(floatValue[0])
        }
    }

    /** One slider per component; labels get " - x/y/z" suffixes since ImGui labels must be unique per window. */
    private fun vectorSliders(label: String, vector: Vector3f, min: Float, max: Float) {
        sliderFloat("$label - x", vector.x, min, max) { vector.x = it }
        sliderFloat("$label - y", vector.y, min, max) { vector.y = it }
        sliderFloat("$label - z", vector.z, min, max) { vector.z = it }
    }

    private fun colorEdit(label: String, color: Vector3f) {
        colorValue[0] = color.x
        colorValue[1] = color.y
        colorValue[2] = color.z
        if (ImGui.colorEdit3(label, colorValue)) {
            color.set(colorValue[0], colorValue[1], colorValue[2])
        }
    }
}

package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.graphics.Model
import com.johnmbergman.iceberg.graphics.Projection
import com.johnmbergman.iceberg.scene.lighting.Light

class Scene {
    val environment = Environment()
    var skyBox: SkyBox? = null
    var fog: Fog = Fog()
    val lights: List<Light>
        field = mutableListOf<Light>()

    val modelMap = mutableMapOf<String, Model>()
    val projection = Projection()
    val camera = Camera()

    fun addEntity(entity: Entity) {
        val model = modelMap[entity.modelId] ?: throw RuntimeException("Could not find model: ${entity.modelId}")
        model.addEntity(entity)
    }

    fun addModel(model: Model) {
        modelMap[model.id] = model
    }

    fun addLight(light: Light) {
        lights += light
    }

    fun removeLight(light: Light) {
        lights -= light
    }

    fun cleanup() {
        modelMap.values.forEach { it.cleanup() }
        skyBox?.cleanup()
    }

    fun resize(width: Int, height: Int) {
        projection.updateProjectionMatrix(width, height)
    }
}
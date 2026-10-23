package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.graphics.Model
import com.johnmbergman.iceberg.graphics.Projection

class Scene {
    val modelMap = mutableMapOf<String, Model>()
    val projection = Projection()

    fun addEntity(entity: Entity) {
        val model = modelMap[entity.modelId] ?: throw RuntimeException("Could not find model: ${entity.modelId}")
        model.addEntity(entity)
    }

    fun addModel(model: Model) {
        modelMap[model.id] = model
    }

    fun cleanup() {
        modelMap.values.forEach { it.cleanup() }
    }

    fun resize(width: Int, height: Int) {
        projection.updateProjectionMatrix(width, height)
    }
}
package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.scene.Entity

class Model(val id: String, val meshList: List<Mesh>) {
    private val entitiesList = mutableListOf<Entity>()

    fun cleanup() {
        meshList.forEach { it.cleanup() }
    }

    fun getEntities(): List<Entity> {
        return entitiesList.toList()
    }

    fun addEntity(entity: Entity) {
        entitiesList.add(entity)
    }
}
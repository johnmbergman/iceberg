package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.graphics.Mesh

class Scene {
    val meshMap = mutableMapOf<String, Mesh>()

    fun addMesh(meshId: String, mesh: Mesh) {
        meshMap[meshId] = mesh
    }

    fun cleanup() {
        meshMap.values.forEach { it.cleanup() }
    }
}
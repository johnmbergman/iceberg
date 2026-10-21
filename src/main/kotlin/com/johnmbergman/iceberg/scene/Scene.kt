package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.graphics.Mesh
import com.johnmbergman.iceberg.graphics.Projection

class Scene {
    val meshMap = mutableMapOf<String, Mesh>()
    val projection = Projection()

    fun addMesh(meshId: String, mesh: Mesh) {
        meshMap[meshId] = mesh
    }

    fun cleanup() {
        meshMap.values.forEach { it.cleanup() }
    }

    fun resize(width: Int, height: Int) {
        projection.updateProjectionMatrix(width, height)
    }
}
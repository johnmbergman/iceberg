package com.johnmbergman.iceberg.graphics

class Material(val texture: Texture) {
    val meshList = mutableListOf<Mesh>()

    fun cleanup() {
        meshList.forEach { it.cleanup() }
    }
}
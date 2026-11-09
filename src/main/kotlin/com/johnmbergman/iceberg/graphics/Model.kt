package com.johnmbergman.iceberg.graphics

/** A model on the GPU. Owned by [ResourceCache]; scene nodes refer to it through a [com.johnmbergman.iceberg.scene.ModelId]. */
class Model(val id: String, val materialList: List<Material>) {

    fun cleanup() {
        materialList.forEach { it.cleanup() }
    }
}

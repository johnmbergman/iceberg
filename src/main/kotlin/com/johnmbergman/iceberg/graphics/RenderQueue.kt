package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.scene.ModelId
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.scene.SceneNode
import org.joml.Matrix4fc

/**
 * The scene's drawable nodes for one frame, grouped by model so the renderer binds each model's materials and
 * meshes once and draws them for every node that uses it. Rebuilt each frame by walking the scene graph; makes no
 * OpenGL calls. The per-model lists are reused between frames rather than reallocated.
 */
class RenderQueue {

    /**
     * Each model used by the scene, with the world matrices of the nodes that draw it, in scene-graph order.
     * The matrices are live views of the nodes' world matrices, valid until the scene next changes.
     */
    val batches: Map<ModelId, List<Matrix4fc>>
        field = LinkedHashMap<ModelId, MutableList<Matrix4fc>>()

    fun build(scene: Scene) {
        batches.values.forEach { it.clear() }
        collect(scene.root)
        // Forget models that no node draws any more
        batches.values.removeIf { it.isEmpty() }
    }

    private fun collect(node: SceneNode) {
        node.modelId?.let { modelId -> batches.getOrPut(modelId) { mutableListOf() }.add(node.worldMatrix) }
        node.children.forEach(::collect)
    }
}

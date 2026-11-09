package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.scene.lighting.Light

/** Everything in the world: the node tree, lights, camera and scene-wide settings. Holds no GPU resources. */
class Scene {
    val environment = Environment()
    val lights: List<Light>
        field = mutableListOf<Light>()

    val camera = Camera()

    /** Every node in the scene descends from this one. */
    val root = SceneNode("root")

    /** Adds a top-level node. To nest nodes, call [SceneNode.addChild] on the parent instead. */
    fun add(node: SceneNode) {
        root.addChild(node)
    }

    fun remove(node: SceneNode) {
        root.removeChild(node)
    }

    fun addLight(light: Light) {
        lights += light
    }

    fun removeLight(light: Light) {
        lights -= light
    }

    fun resize(width: Int, height: Int) {
        camera.projection.updateProjectionMatrix(width, height)
    }
}

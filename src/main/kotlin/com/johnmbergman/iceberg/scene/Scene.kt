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

    /** Calls [action] for every [T] on a node under [root], in scene-graph order (parents before children). */
    inline fun <reified T : Component> forEach(noinline action: (T) -> Unit) = forEach(T::class.java, action)

    fun <T : Component> forEach(type: Class<T>, action: (T) -> Unit) = visit(root, type, action)

    private fun <T : Component> visit(node: SceneNode, type: Class<T>, action: (T) -> Unit) {
        // Indexed loops: this runs every frame, so avoid creating iterators
        val components = node.components
        for (i in components.indices) {
            val component = components[i]
            if (type.isInstance(component)) {
                action(type.cast(component))
            }
        }
        val children = node.children
        for (i in children.indices) {
            visit(children[i], type, action)
        }
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

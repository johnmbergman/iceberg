package com.johnmbergman.iceberg.scene

/**
 * Data attached to a [SceneNode], giving it a role such as drawing a model. A node holds at most one component of
 * each concrete class. Systems find components with [Scene.forEach].
 */
abstract class Component {
    /** The node this is attached to, or null when detached. Set by [SceneNode.add] and [SceneNode.remove]. */
    var node: SceneNode? = null
        internal set
}

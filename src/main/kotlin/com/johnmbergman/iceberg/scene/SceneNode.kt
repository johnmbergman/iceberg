package com.johnmbergman.iceberg.scene

import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * A node in the scene graph. Its transform (position, rotation and per-axis scale, applied as scale, then rotation,
 * then translation) is relative to its parent, and [worldMatrix] combines it with every ancestor's. Matrices are
 * recomputed lazily, only after something changed, so callers never need to update them.
 *
 * What a node does beyond positioning its children comes from its [components]; for example, a [ModelRenderer]
 * draws a model. A node without components just groups and positions its children.
 */
class SceneNode(val name: String = "") {

    val components: List<Component>
        field = ArrayList<Component>(2)

    var parent: SceneNode? = null
        private set
    val children: List<SceneNode>
        field = mutableListOf<SceneNode>()

    // Read-only views; change them through the setters below so the matrices know to update
    val position: Vector3fc field = Vector3f()
    val rotation: Quaternionfc field = Quaternionf()
    val scale: Vector3fc field = Vector3f(1f)

    // Both start as the identity, which matches the default transform and having no parent
    private val local = Matrix4f()
    private val world = Matrix4f()
    private var localDirty = false
    // Invariant: a node with a dirty world matrix has only dirty descendants, since their world matrices depend
    // on it. That lets markWorldDirty stop at nodes that are already dirty.
    private var worldDirty = false

    /** This node's transform relative to its parent. The returned matrix is a live view. */
    val localMatrix: Matrix4fc
        get() {
            if (localDirty) {
                local.translationRotateScale(position, rotation, scale)
                localDirty = false
            }
            return local
        }

    /** This node's transform relative to the scene root. The returned matrix is a live view. */
    val worldMatrix: Matrix4fc
        get() {
            if (worldDirty) {
                val parent = parent
                if (parent == null) {
                    world.set(localMatrix)
                } else {
                    parent.worldMatrix.mul(localMatrix, world)
                }
                worldDirty = false
            }
            return world
        }

    fun setPosition(x: Float, y: Float, z: Float) {
        position.set(x, y, z)
        localChanged()
    }

    fun setPosition(position: Vector3fc) {
        setPosition(position.x(), position.y(), position.z())
    }

    /** Rotates [angleInRadians] about the axis (x, y, z), replacing any previous rotation. */
    fun setRotation(x: Float, y: Float, z: Float, angleInRadians: Float) {
        rotation.fromAxisAngleRad(x, y, z, angleInRadians)
        localChanged()
    }

    fun setRotation(rotation: Quaternionfc) {
        this.rotation.set(rotation)
        localChanged()
    }

    /** Scales along the node's local x, y and z axes. */
    fun setScale(x: Float, y: Float, z: Float) {
        scale.set(x, y, z)
        localChanged()
    }

    fun setScale(scale: Vector3fc) {
        setScale(scale.x(), scale.y(), scale.z())
    }

    /** Scales equally along every axis. */
    fun setScale(uniform: Float) {
        setScale(uniform, uniform, uniform)
    }

    /** Adds [child] as the last child, first removing it from any previous parent. */
    fun addChild(child: SceneNode) {
        require(child !== this) { "Node '$name' can't be its own child" }
        var ancestor = parent
        while (ancestor != null) {
            require(ancestor !== child) { "Adding '${child.name}' under '$name' would create a cycle; it's an ancestor" }
            ancestor = ancestor.parent
        }
        child.parent?.removeChild(child)
        child.parent = this
        children += child
        child.markWorldDirty()
    }

    fun removeChild(child: SceneNode) {
        require(child.parent === this) { "Node '${child.name}' is not a child of '$name'" }
        children.remove(child)
        child.parent = null
        child.markWorldDirty()
    }

    /** Attaches [component] and returns it. A node can't hold two components of the same class. */
    fun <T : Component> add(component: T): T {
        require(component.node == null) {
            "${component.javaClass.simpleName} is already attached to '${component.node?.name}'"
        }
        require(components.none { it.javaClass == component.javaClass }) {
            "Node '$name' already has a ${component.javaClass.simpleName}"
        }
        components += component
        component.node = this
        return component
    }

    fun remove(component: Component) {
        require(component.node === this) { "${component.javaClass.simpleName} is not attached to '$name'" }
        components.remove(component)
        component.node = null
    }

    /** The first component that is a [T]. Subtypes match, so asking for a supertype finds any of its subclasses. */
    inline fun <reified T : Component> get(): T? = components.firstOrNull { it is T } as T?

    private fun localChanged() {
        localDirty = true
        markWorldDirty()
    }

    private fun markWorldDirty() {
        if (worldDirty) {
            return
        }
        worldDirty = true
        children.forEach { it.markWorldDirty() }
    }

    override fun toString() = "SceneNode($name)"
}

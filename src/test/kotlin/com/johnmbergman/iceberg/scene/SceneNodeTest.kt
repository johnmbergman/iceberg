package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.TOLERANCE
import com.johnmbergman.iceberg.assertVectorEquals
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val QUARTER_TURN = (PI / 2).toFloat()

class SceneNodeTest {

    /** Where the node's local origin ends up in world space. */
    private fun SceneNode.worldOrigin(): Vector3fc = worldMatrix.transformPosition(Vector3f())

    private fun assertIdentity(matrix: Matrix4fc) {
        assertTrue(Matrix4f(matrix).equals(Matrix4f(), TOLERANCE), "Expected identity but was\n$matrix")
    }

    // Defaults

    @Test
    fun startsDetachedWithIdentityMatrices() {
        val node = SceneNode("node")
        assertNull(node.parent)
        assertTrue(node.children.isEmpty())
        assertNull(node.modelId)
        assertIdentity(node.localMatrix)
        assertIdentity(node.worldMatrix)
    }

    // Every mutator updates the matrices without an explicit update call

    @Test
    fun setPositionUpdatesMatricesImmediately() {
        val node = SceneNode()
        node.worldMatrix // Read first, so a stale cached value would show
        node.setPosition(1f, 2f, 3f)
        assertVectorEquals(1f, 2f, 3f, node.worldOrigin())
    }

    @Test
    fun setPositionFromVectorUpdatesMatrices() {
        val node = SceneNode()
        node.worldMatrix
        node.setPosition(Vector3f(4f, 5f, 6f))
        assertVectorEquals(4f, 5f, 6f, node.position)
        assertVectorEquals(4f, 5f, 6f, node.worldOrigin())
    }

    @Test
    fun setRotationFromAxisAngleUpdatesMatrices() {
        val node = SceneNode()
        node.worldMatrix
        node.setRotation(0f, 1f, 0f, QUARTER_TURN)
        assertVectorEquals(0f, 0f, -1f, node.worldMatrix.transformPosition(Vector3f(1f, 0f, 0f)))
    }

    @Test
    fun setRotationFromQuaternionUpdatesMatrices() {
        val node = SceneNode()
        node.worldMatrix
        node.setRotation(Quaternionf().rotationY(QUARTER_TURN))
        assertVectorEquals(0f, 0f, -1f, node.worldMatrix.transformPosition(Vector3f(1f, 0f, 0f)))
    }

    @Test
    fun uniformScaleUpdatesMatrices() {
        val node = SceneNode()
        node.worldMatrix
        node.setScale(3f)
        assertVectorEquals(3f, 3f, 3f, node.scale)
        assertVectorEquals(3f, 3f, 3f, node.worldMatrix.transformPosition(Vector3f(1f, 1f, 1f)))
    }

    @Test
    fun perAxisScaleUpdatesMatrices() {
        val node = SceneNode()
        node.worldMatrix
        node.setScale(2f, 3f, 4f)
        assertVectorEquals(2f, 3f, 4f, node.worldMatrix.transformPosition(Vector3f(1f, 1f, 1f)))
    }

    @Test
    fun setScaleFromVectorUpdatesMatrices() {
        val node = SceneNode()
        node.worldMatrix
        node.setScale(Vector3f(2f, 3f, 4f))
        assertVectorEquals(2f, 3f, 4f, node.scale)
        assertVectorEquals(2f, 3f, 4f, node.worldMatrix.transformPosition(Vector3f(1f, 1f, 1f)))
    }

    @Test
    fun scaleIsAlongLocalAxesBeforeRotation() {
        val node = SceneNode()
        node.setScale(2f, 1f, 1f)
        node.setRotation(0f, 1f, 0f, QUARTER_TURN) // Maps +X to -Z and +Z to +X

        // Stretched along local x before turning, so the stretch ends up along world z
        assertVectorEquals(0f, 0f, -2f, node.worldMatrix.transformPosition(Vector3f(1f, 0f, 0f)))
        assertVectorEquals(1f, 0f, 0f, node.worldMatrix.transformPosition(Vector3f(0f, 0f, 1f)))
    }

    @Test
    fun appliesScaleThenRotationThenTranslation() {
        val node = SceneNode()
        node.setScale(2f)
        node.setRotation(0f, 1f, 0f, QUARTER_TURN) // A quarter turn about +Y maps +X to -Z
        node.setPosition(10f, 0f, 0f)

        // (1,0,0) → scaled (2,0,0) → rotated (0,0,-2) → translated (10,0,-2)
        assertVectorEquals(10f, 0f, -2f, node.worldMatrix.transformPosition(Vector3f(1f, 0f, 0f)))
    }

    @Test
    fun matricesAreLiveViews() {
        val node = SceneNode()
        val world = node.worldMatrix
        node.setPosition(1f, 0f, 0f)
        assertSame(world, node.worldMatrix)
    }

    // Hierarchy

    @Test
    fun childWorldMatrixCombinesParentAndLocal() {
        val parent = SceneNode("parent")
        parent.setPosition(10f, 0f, 0f)
        parent.setRotation(0f, 1f, 0f, QUARTER_TURN)
        val child = SceneNode("child")
        child.setPosition(1f, 0f, 0f)
        parent.addChild(child)

        // The parent's rotation turns the child's offset (1,0,0) into (0,0,-1)
        assertVectorEquals(10f, 0f, -1f, child.worldOrigin())
        // The child's local matrix ignores the parent
        assertVectorEquals(1f, 0f, 0f, child.localMatrix.transformPosition(Vector3f()))
    }

    @Test
    fun parentScaleScalesChildOffset() {
        val parent = SceneNode("parent")
        parent.setScale(2f)
        val child = SceneNode("child")
        child.setPosition(1f, 0f, 0f)
        parent.addChild(child)

        assertVectorEquals(2f, 0f, 0f, child.worldOrigin())
    }

    @Test
    fun movingParentMovesChild() {
        val parent = SceneNode("parent")
        val child = SceneNode("child")
        child.setPosition(1f, 0f, 0f)
        parent.addChild(child)
        assertVectorEquals(1f, 0f, 0f, child.worldOrigin())

        parent.setPosition(0f, 5f, 0f)
        assertVectorEquals(1f, 5f, 0f, child.worldOrigin())
    }

    @Test
    fun changingParentTwiceBeforeReadingChildIsStillCorrect() {
        // The second change finds the parent already dirty and stops propagating early
        val parent = SceneNode("parent")
        val child = SceneNode("child")
        parent.addChild(child)
        child.worldMatrix

        parent.setPosition(1f, 0f, 0f)
        parent.setPosition(2f, 0f, 0f)
        assertVectorEquals(2f, 0f, 0f, child.worldOrigin())
    }

    @Test
    fun changesPropagateToGrandchildren() {
        val grandparent = SceneNode("grandparent")
        val parent = SceneNode("parent")
        val child = SceneNode("child")
        grandparent.addChild(parent)
        parent.addChild(child)
        child.worldMatrix

        grandparent.setPosition(1f, 0f, 0f)
        grandparent.setPosition(2f, 0f, 0f)
        assertVectorEquals(2f, 0f, 0f, child.worldOrigin())
    }

    @Test
    fun readingParentDoesNotHideAPendingChildUpdate() {
        val grandparent = SceneNode("grandparent")
        val parent = SceneNode("parent")
        val child = SceneNode("child")
        grandparent.addChild(parent)
        parent.addChild(child)
        child.worldMatrix

        grandparent.setPosition(1f, 0f, 0f)
        parent.worldMatrix // Cleans the parent while the child is still dirty
        grandparent.setPosition(2f, 0f, 0f)
        assertVectorEquals(2f, 0f, 0f, child.worldOrigin())
    }

    @Test
    fun changingChildThenParentCombinesBoth() {
        val parent = SceneNode("parent")
        val child = SceneNode("child")
        parent.addChild(child)
        child.worldMatrix

        child.setPosition(0f, 1f, 0f)  // Child dirty first
        parent.setPosition(3f, 0f, 0f) // Then the parent; propagation stops at the already-dirty child
        assertVectorEquals(3f, 1f, 0f, child.worldOrigin())
    }

    @Test
    fun reparentingUsesTheNewParent() {
        val first = SceneNode("first")
        first.setPosition(1f, 0f, 0f)
        val second = SceneNode("second")
        second.setPosition(0f, 0f, 7f)
        val child = SceneNode("child")
        first.addChild(child)
        assertVectorEquals(1f, 0f, 0f, child.worldOrigin())

        second.addChild(child)
        assertSame(second, child.parent)
        assertTrue(child !in first.children)
        assertEquals(listOf(child), second.children)
        assertVectorEquals(0f, 0f, 7f, child.worldOrigin())
    }

    @Test
    fun reparentingUpdatesTheMovedSubtree() {
        val first = SceneNode("first")
        val second = SceneNode("second")
        second.setPosition(5f, 0f, 0f)
        val child = SceneNode("child")
        val grandchild = SceneNode("grandchild")
        first.addChild(child)
        child.addChild(grandchild)
        grandchild.worldMatrix

        second.addChild(child)
        assertVectorEquals(5f, 0f, 0f, grandchild.worldOrigin())
    }

    @Test
    fun removedChildFallsBackToItsLocalTransform() {
        val parent = SceneNode("parent")
        parent.setPosition(10f, 0f, 0f)
        val child = SceneNode("child")
        child.setPosition(1f, 0f, 0f)
        parent.addChild(child)
        assertVectorEquals(11f, 0f, 0f, child.worldOrigin())

        parent.removeChild(child)
        assertNull(child.parent)
        assertTrue(parent.children.isEmpty())
        assertVectorEquals(1f, 0f, 0f, child.worldOrigin())
    }

    @Test
    fun addingAnExistingChildAgainMovesItToTheEnd() {
        val parent = SceneNode("parent")
        val a = SceneNode("a")
        val b = SceneNode("b")
        parent.addChild(a)
        parent.addChild(b)

        parent.addChild(a)
        assertEquals(listOf(b, a), parent.children)
    }

    // Invalid trees

    @Test
    fun nodeCannotBeItsOwnChild() {
        val node = SceneNode("node")
        assertFailsWith<IllegalArgumentException> { node.addChild(node) }
    }

    @Test
    fun ancestorCannotBecomeAChild() {
        val grandparent = SceneNode("grandparent")
        val parent = SceneNode("parent")
        val child = SceneNode("child")
        grandparent.addChild(parent)
        parent.addChild(child)

        assertFailsWith<IllegalArgumentException> { child.addChild(parent) }
        assertFailsWith<IllegalArgumentException> { child.addChild(grandparent) }
        // The failed calls left the tree unchanged
        assertSame(grandparent, parent.parent)
        assertNull(grandparent.parent)
    }

    @Test
    fun removingANonChildFails() {
        val parent = SceneNode("parent")
        val stranger = SceneNode("stranger")
        assertFailsWith<IllegalArgumentException> { parent.removeChild(stranger) }
    }
}

package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.assertVectorEquals
import com.johnmbergman.iceberg.scene.ModelId
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.scene.SceneNode
import org.joml.Vector3f
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** RenderQueue makes no OpenGL calls, so it can be tested without a context. */
class RenderQueueTest {

    private val wall = ModelId("models/wall/wall.obj")
    private val cube = ModelId("models/cube/cube.obj")

    private fun node(name: String, modelId: ModelId? = null) = SceneNode(name).also { it.modelId = modelId }

    @Test
    fun emptySceneHasNoBatches() {
        val queue = RenderQueue()
        queue.build(Scene())
        assertTrue(queue.batches.isEmpty())
    }

    @Test
    fun groupsNodesByModelInSceneGraphOrder() {
        val scene = Scene()
        val wallA = node("wall-a", wall)
        val cubeA = node("cube-a", cube)
        val wallB = node("wall-b", wall)
        scene.add(wallA)
        scene.add(cubeA)
        scene.add(wallB)

        val queue = RenderQueue()
        queue.build(scene)

        assertEquals(listOf(wall, cube), queue.batches.keys.toList())
        assertEquals(listOf(wallA.worldMatrix, wallB.worldMatrix), queue.batches.getValue(wall))
        assertEquals(listOf(cubeA.worldMatrix), queue.batches.getValue(cube))
    }

    @Test
    fun includesNestedNodesAndSkipsNodesWithoutAModel() {
        val scene = Scene()
        val group = node("group") // No model: only positions its children
        group.setPosition(10f, 0f, 0f)
        val nested = node("nested", wall)
        nested.setPosition(1f, 0f, 0f)
        group.addChild(nested)
        scene.add(group)

        val queue = RenderQueue()
        queue.build(scene)

        val matrices = queue.batches.getValue(wall)
        assertEquals(1, matrices.size)
        assertSame(nested.worldMatrix, matrices.single())
        // The parent's transform is applied
        assertVectorEquals(11f, 0f, 0f, matrices.single().transformPosition(Vector3f()))
    }

    @Test
    fun rebuildingDoesNotDuplicateNodes() {
        val scene = Scene()
        scene.add(node("wall", wall))

        val queue = RenderQueue()
        queue.build(scene)
        queue.build(scene)

        assertEquals(1, queue.batches.getValue(wall).size)
    }

    @Test
    fun rebuildingDropsRemovedNodesAndUnusedModels() {
        val scene = Scene()
        val wallNode = node("wall", wall)
        val cubeNode = node("cube", cube)
        scene.add(wallNode)
        scene.add(cubeNode)

        val queue = RenderQueue()
        queue.build(scene)
        scene.remove(cubeNode)
        queue.build(scene)

        assertEquals(setOf(wall), queue.batches.keys)
    }

    @Test
    fun picksUpModelChangesOnTheNextBuild() {
        val scene = Scene()
        val swapped = node("swapped", wall)
        scene.add(swapped)

        val queue = RenderQueue()
        queue.build(scene)
        swapped.modelId = cube
        queue.build(scene)

        assertEquals(setOf(cube), queue.batches.keys)
    }
}

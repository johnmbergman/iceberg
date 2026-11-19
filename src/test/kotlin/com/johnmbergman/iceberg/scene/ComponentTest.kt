package com.johnmbergman.iceberg.scene

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ComponentTest {

    private open class StubComponent(val label: String = "") : Component()
    private class DerivedStubComponent(label: String = "") : StubComponent(label)

    private val wall = ModelId("models/wall/wall.obj")

    // Attaching and detaching

    @Test
    fun addAttachesAndReturnsTheComponent() {
        val node = SceneNode("node")
        val renderer = ModelRenderer(wall)
        assertSame(renderer, node.add(renderer))
        assertSame(node, renderer.node)
        assertEquals(listOf<Component>(renderer), node.components)
    }

    @Test
    fun removeDetaches() {
        val node = SceneNode("node")
        val renderer = node.add(ModelRenderer(wall))
        node.remove(renderer)
        assertNull(renderer.node)
        assertTrue(node.components.isEmpty())
    }

    @Test
    fun aRemovedComponentCanBeAddedElsewhere() {
        val first = SceneNode("first")
        val second = SceneNode("second")
        val renderer = first.add(ModelRenderer(wall))
        first.remove(renderer)
        second.add(renderer)
        assertSame(second, renderer.node)
    }

    @Test
    fun rejectsAComponentAttachedToAnotherNode() {
        val renderer = SceneNode("first").add(ModelRenderer(wall))
        assertFailsWith<IllegalArgumentException> { SceneNode("second").add(renderer) }
    }

    @Test
    fun rejectsASecondComponentOfTheSameClass() {
        val node = SceneNode("node")
        node.add(ModelRenderer(wall))
        assertFailsWith<IllegalArgumentException> { node.add(ModelRenderer(wall)) }
    }

    @Test
    fun allowsASubclassAlongsideItsSuperclass() {
        val node = SceneNode("node")
        node.add(StubComponent())
        node.add(DerivedStubComponent())
        assertEquals(2, node.components.size)
    }

    @Test
    fun rejectsRemovingAComponentFromTheWrongNode() {
        val renderer = SceneNode("first").add(ModelRenderer(wall))
        assertFailsWith<IllegalArgumentException> { SceneNode("second").remove(renderer) }
    }

    // Lookup

    @Test
    fun getFindsByClassOrNull() {
        val node = SceneNode("node")
        assertNull(node.get<ModelRenderer>())
        val renderer = node.add(ModelRenderer(wall))
        assertSame(renderer, node.get<ModelRenderer>())
        assertNull(node.get<StubComponent>())
    }

    @Test
    fun getMatchesSubtypes() {
        val node = SceneNode("node")
        val special = node.add(DerivedStubComponent())
        assertSame(special, node.get<StubComponent>())
    }

    // Scene.forEach

    @Test
    fun forEachVisitsMatchingComponentsInSceneGraphOrder() {
        val scene = Scene()
        val a = SceneNode("a").also { it.add(StubComponent("a")) }
        val aChild = SceneNode("a-child").also { it.add(DerivedStubComponent("a-child")) }
        val group = SceneNode("group") // No components
        val b = SceneNode("b").also { it.add(StubComponent("b")) }
        a.addChild(aChild)
        group.addChild(b)
        scene.add(a)
        scene.add(group)

        val labels = mutableListOf<String>()
        scene.forEach<StubComponent> { labels += it.label }

        assertEquals(listOf("a", "a-child", "b"), labels)
    }

    @Test
    fun forEachFiltersByType() {
        val scene = Scene()
        val node = SceneNode("node")
        val renderer = node.add(ModelRenderer(wall))
        node.add(StubComponent())
        scene.add(node)

        val visited = mutableListOf<ModelRenderer>()
        scene.forEach<ModelRenderer> { visited += it }

        assertEquals(listOf(renderer), visited)
    }

    @Test
    fun forEachSkipsNodesOutsideTheScene() {
        val scene = Scene()
        val removed = SceneNode("removed").also { it.add(StubComponent()) }
        scene.add(removed)
        scene.remove(removed)
        SceneNode("never-added").add(StubComponent())

        var count = 0
        scene.forEach<StubComponent> { count++ }

        assertEquals(0, count)
    }
}

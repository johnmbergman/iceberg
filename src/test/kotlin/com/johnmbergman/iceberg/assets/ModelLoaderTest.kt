package com.johnmbergman.iceberg.assets

import com.johnmbergman.iceberg.TOLERANCE
import com.johnmbergman.iceberg.math.Color
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Reads the sample models with Assimp. No OpenGL context is involved. */
class ModelLoaderTest {

    /** Assimp's OBJ importer can add an unused default material, so find the one that has meshes. */
    private fun ModelData.onlyMaterialWithMeshes(): MaterialData {
        val used = materials.filter { it.meshes.isNotEmpty() }
        assertEquals(1, used.size, "Expected exactly one material with meshes, found ${used.size}")
        return used.single()
    }

    private fun MaterialData.onlyMesh(): MeshData {
        assertEquals(1, meshes.size, "Expected exactly one mesh, found ${meshes.size}")
        return meshes.single()
    }

    private fun assertColorEquals(expected: Color, actual: Color) {
        assertEquals(expected.r, actual.r, TOLERANCE, "red")
        assertEquals(expected.g, actual.g, TOLERANCE, "green")
        assertEquals(expected.b, actual.b, TOLERANCE, "blue")
    }

    // wall.obj: a 2×2 quad in the XZ plane, two triangles, one normal (0, 1, 0)

    @Test
    fun wallIsAQuadOfTwoTriangles() {
        val mesh = ModelLoader.load("wall", "models/wall/wall.obj").onlyMaterialWithMeshes().onlyMesh()

        assertEquals(4, mesh.vertexCount)
        assertEquals(6, mesh.indices.size)
        assertTrue(mesh.indices.all { it in 0 until mesh.vertexCount }, "Indices out of range: ${mesh.indices.toList()}")
        assertEquals(mesh.vertexCount * 3, mesh.normals.size)
        assertEquals(mesh.vertexCount * 3, mesh.tangents.size)
        assertEquals(mesh.vertexCount * 3, mesh.bitangents.size)
        assertEquals(mesh.vertexCount * 2, mesh.textureCoordinates.size)
    }

    @Test
    fun wallNormalsPointUp() {
        val mesh = ModelLoader.load("wall", "models/wall/wall.obj").onlyMaterialWithMeshes().onlyMesh()

        for (vertex in 0 until mesh.vertexCount) {
            assertEquals(0f, mesh.normals[vertex * 3], TOLERANCE)
            assertEquals(1f, mesh.normals[vertex * 3 + 1], TOLERANCE)
            assertEquals(0f, mesh.normals[vertex * 3 + 2], TOLERANCE)
        }
    }

    @Test
    fun wallHasTangentsBecauseItHasTextureCoordinates() {
        val mesh = ModelLoader.load("wall", "models/wall/wall.obj").onlyMaterialWithMeshes().onlyMesh()

        assertTrue(mesh.tangents.any { it != 0f }, "Tangents are all zero")
        assertTrue(mesh.bitangents.any { it != 0f }, "Bitangents are all zero")
    }

    @Test
    fun textureCoordinatesAreFlippedVertically() {
        val mesh = ModelLoader.load("wall", "models/wall/wall.obj").onlyMaterialWithMeshes().onlyMesh()

        // From wall.obj: position (x, z) → vt (u, v). Expect (u, 1 - v), matched by position since Assimp may reorder.
        val expected = mapOf(
            (1f to 1f) to (1f to 1f),    // vt 1 0
            (-1f to 1f) to (0f to 1f),   // vt 0 0
            (-1f to -1f) to (0f to 0f),  // vt 0 1
            (1f to -1f) to (1f to 0f),   // vt 1 1
        )
        for (vertex in 0 until mesh.vertexCount) {
            val position = mesh.positions[vertex * 3] to mesh.positions[vertex * 3 + 2]
            val (u, v) = assertNotNull(expected[position], "Unexpected vertex position $position")
            assertEquals(u, mesh.textureCoordinates[vertex * 2], TOLERANCE, "u at $position")
            assertEquals(v, mesh.textureCoordinates[vertex * 2 + 1], TOLERANCE, "v at $position")
        }
    }

    @Test
    fun wallMaterialHasTexturesAndColorsFromItsMtlFile() {
        val material = ModelLoader.load("wall", "models/wall/wall.obj").onlyMaterialWithMeshes()

        assertEquals("models/wall/wall.png", material.diffuseTexturePath)
        assertEquals("models/wall/wall_normals.png", material.normalMapPath)
        assertColorEquals(Color(0.59f, 0.45f, 0.35f), material.ambientColor)  // Ka
        assertColorEquals(Color(0.59f, 0.45f, 0.35f), material.specularColor) // Ks
        // Textured, so Kd is ignored and the default stays
        assertEquals(MaterialData.DEFAULT_COLOR, material.diffuseColor)
    }

    @Test
    fun wallWithoutNormalMapHasNoNormalMapPath() {
        val material = ModelLoader.load("wall", "models/wall/wall_nonormals.obj").onlyMaterialWithMeshes()

        assertEquals("models/wall/wall.png", material.diffuseTexturePath)
        assertNull(material.normalMapPath)
    }

    @Test
    fun cubeHasTwentyFourVerticesAndTwelveTriangles() {
        // 24 unique position/texture-coordinate/normal combinations in cube.obj; 12 triangular faces
        val mesh = ModelLoader.load("cube", "models/cube/cube.obj").onlyMaterialWithMeshes().onlyMesh()

        assertEquals(24, mesh.vertexCount)
        assertEquals(36, mesh.indices.size)
    }

    @Test
    fun keepsTheGivenModelId() {
        assertEquals("my-wall", ModelLoader.load("my-wall", "models/wall/wall.obj").id)
    }

    @Test
    fun acceptsALeadingSlashInTheModelPath() {
        val material = ModelLoader.load("wall", "/models/wall/wall.obj").onlyMaterialWithMeshes()
        assertEquals("models/wall/wall.png", material.diffuseTexturePath)
    }

    @Test
    fun missingModelFails() {
        val error = assertFailsWith<RuntimeException> { ModelLoader.load("missing", "models/missing/missing.obj") }
        assertTrue("models/missing/missing.obj" in error.message.orEmpty(), error.message)
    }

    // resolveTexturePath

    @Test
    fun texturePathIsResolvedNextToTheModel() {
        assertEquals("models/wall/wall.png", ModelLoader.resolveTexturePath("models/wall", "wall.png"))
    }

    @Test
    fun texturePathKeepsOnlyTheFileName() {
        assertEquals("models/wall/wall.png", ModelLoader.resolveTexturePath("models/wall", "/Users/artist/textures/wall.png"))
        assertEquals("models/wall/wall.png", ModelLoader.resolveTexturePath("models/wall", "textures/wall.png"))
    }

    @Test
    fun texturePathHandlesWindowsSeparators() {
        assertEquals("models/wall/wall.png", ModelLoader.resolveTexturePath("models/wall", "C:\\textures\\wall.png"))
    }

    @Test
    fun texturePathForAModelAtTheClasspathRoot() {
        assertEquals("wall.png", ModelLoader.resolveTexturePath("", "wall.png"))
    }

    @Test
    fun meshDataVertexCountComesFromPositions() {
        val mesh = MeshData(FloatArray(9), FloatArray(9), FloatArray(9), FloatArray(9), FloatArray(6), IntArray(3))
        assertEquals(3, mesh.vertexCount)
        assertContentEquals(IntArray(3), mesh.indices)
    }
}

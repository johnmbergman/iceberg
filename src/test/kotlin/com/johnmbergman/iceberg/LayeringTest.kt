package com.johnmbergman.iceberg

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Enforces the package layering: dependencies point graphics → assets → scene → math.
 * Lower layers must not import higher ones or OpenGL, so they stay testable without a GL context.
 */
class LayeringTest {

    private val sourceRoot = File("src/main/kotlin/com/johnmbergman/iceberg")

    private val gl = listOf("org.lwjgl.opengl", "org.lwjgl.glfw", "imgui")

    private fun assertNoImports(pkg: String, forbidden: List<String>) {
        val dir = File(sourceRoot, pkg)
        assertTrue(dir.isDirectory, "Missing source directory ${dir.absolutePath}; is the test running from the project root?")

        val violations = dir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines()
                    .filter { it.startsWith("import ") }
                    .filter { line -> forbidden.any { line.removePrefix("import ").startsWith(it) } }
                    .map { "${file.relativeTo(sourceRoot)}: $it" }
            }
            .toList()

        if (violations.isNotEmpty()) {
            fail("Package '$pkg' must not import ${forbidden.joinToString()}:\n" + violations.joinToString("\n"))
        }
    }

    @Test
    fun mathDependsOnNothingInTheEngine() {
        assertNoImports(
            "math",
            listOf("com.johnmbergman.iceberg.scene", "com.johnmbergman.iceberg.assets", "com.johnmbergman.iceberg.graphics", "org.lwjgl") + gl
        )
    }

    @Test
    fun sceneDoesNotDependOnAssetsGraphicsOrOpenGL() {
        assertNoImports(
            "scene",
            listOf("com.johnmbergman.iceberg.assets", "com.johnmbergman.iceberg.graphics", "org.lwjgl") + gl
        )
    }

    @Test
    fun assetsDoNotDependOnGraphicsOrOpenGL() {
        // Assimp (org.lwjgl.assimp) is allowed: it reads files and needs no GL context
        assertNoImports("assets", listOf("com.johnmbergman.iceberg.graphics") + gl)
    }
}

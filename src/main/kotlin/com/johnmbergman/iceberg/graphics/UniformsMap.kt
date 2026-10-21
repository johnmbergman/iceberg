package com.johnmbergman.iceberg.graphics

import org.joml.Matrix4f
import org.lwjgl.opengl.GL20C.glGetUniformLocation
import org.lwjgl.opengl.GL20C.glUniformMatrix4fv
import org.lwjgl.system.MemoryStack



class UniformsMap(private val programId: Int) {
    val uniforms = mutableMapOf<String, Int>()

    fun createUniform(name: String) {
        val uniformLocation = glGetUniformLocation(programId, name)
        if (uniformLocation < 0) {
            throw RuntimeException("Uniform $name not found")
        }
        uniforms[name] = uniformLocation
    }

    fun setUniform(name: String, value: Matrix4f) {
        MemoryStack.stackPush().use { stack ->
            val location: Int = uniforms[name] ?: throw java.lang.RuntimeException("Could not find uniform [$name]")
            glUniformMatrix4fv(location, false, value.get(stack.mallocFloat(16)))
        }
    }
}
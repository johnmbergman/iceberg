package com.johnmbergman.iceberg.graphics

import org.joml.Matrix4fc
import org.lwjgl.opengl.GL20C.glGetUniformLocation
import org.lwjgl.opengl.GL20C.glUniform1i
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

    fun setUniform(name: String, value: Matrix4fc) {
        MemoryStack.stackPush().use { stack ->
            glUniformMatrix4fv(getLocation(name), false, value.get(stack.mallocFloat(16)))
        }
    }

    fun setUniform(name: String, value: Int) {
        glUniform1i(getLocation(name), value)
    }

    private fun getLocation(name: String): Int {
        return uniforms[name] ?: throw RuntimeException("Could not find uniform [$name]")
    }
}
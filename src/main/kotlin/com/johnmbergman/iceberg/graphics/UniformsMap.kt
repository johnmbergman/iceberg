package com.johnmbergman.iceberg.graphics

import org.joml.Matrix4fc
import org.joml.Vector4f
import org.lwjgl.opengl.GL20C.glGetUniformLocation
import org.lwjgl.opengl.GL20C.glUniform1i
import org.lwjgl.opengl.GL20C.glUniform4f
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

    fun setUniform(name: String, value: Vector4f) {
        glUniform4f(getLocation(name), value.x, value.y, value.z, value.w)
    }

    private fun getLocation(name: String): Int {
        return uniforms[name] ?: throw RuntimeException("Could not find uniform [$name]")
    }
}
package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.math.Color
import org.joml.Matrix4fc
import org.joml.Vector2fc
import org.joml.Vector3fc
import org.lwjgl.opengl.GL20C.glGetUniformLocation
import org.lwjgl.opengl.GL20C.glUniform1f
import org.lwjgl.opengl.GL20C.glUniform1i
import org.lwjgl.opengl.GL20C.glUniform2f
import org.lwjgl.opengl.GL20C.glUniform3f
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

    fun setUniform(name: String, value: Float) {
        glUniform1f(getLocation(name), value)
    }

    fun setUniform(name: String, value: Vector2fc) {
        glUniform2f(getLocation(name), value.x(), value.y())
    }

    fun setUniform(name: String, value: Vector3fc) {
        glUniform3f(getLocation(name), value.x(), value.y(), value.z())
    }

    /** Uploads all four channels; use for `vec4` uniforms. */
    fun setUniform(name: String, value: Color) {
        glUniform4f(getLocation(name), value.r, value.g, value.b, value.a)
    }

    /** Uploads red, green and blue only; use for `vec3` uniforms. */
    fun setUniformRgb(name: String, value: Color) {
        glUniform3f(getLocation(name), value.r, value.g, value.b)
    }

    private fun getLocation(name: String): Int {
        return uniforms[name] ?: throw RuntimeException("Could not find uniform [$name]")
    }
}
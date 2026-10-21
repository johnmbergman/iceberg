package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.utils.Utils
import org.lwjgl.opengl.GL20C.GL_COMPILE_STATUS
import org.lwjgl.opengl.GL20C.GL_LINK_STATUS
import org.lwjgl.opengl.GL20C.GL_VALIDATE_STATUS
import org.lwjgl.opengl.GL20C.glAttachShader
import org.lwjgl.opengl.GL20C.glCompileShader
import org.lwjgl.opengl.GL20C.glCreateProgram
import org.lwjgl.opengl.GL20C.glCreateShader
import org.lwjgl.opengl.GL20C.glDeleteProgram
import org.lwjgl.opengl.GL20C.glDeleteShader
import org.lwjgl.opengl.GL20C.glDetachShader
import org.lwjgl.opengl.GL20C.glGetProgramInfoLog
import org.lwjgl.opengl.GL20C.glGetProgrami
import org.lwjgl.opengl.GL20C.glGetShaderInfoLog
import org.lwjgl.opengl.GL20C.glGetShaderi
import org.lwjgl.opengl.GL20C.glLinkProgram
import org.lwjgl.opengl.GL20C.glShaderSource
import org.lwjgl.opengl.GL20C.glUseProgram
import org.lwjgl.opengl.GL20C.glValidateProgram

class ShaderProgram {
    val programId: Int

    constructor(shaderModuleData: List<ShaderModuleData>) {
        programId = glCreateProgram()
        if (programId == 0) {
            throw RuntimeException("Could not create shader program")
        }
        val shaderModules = ArrayList<Int>()
        shaderModuleData.forEach { shaderModule ->
            val shaderCode = Utils.readResource(shaderModule.shaderFile)
            val shaderId = createShader(shaderCode, shaderModule.shaderType)
            shaderModules.add(shaderId)
        }
        link(shaderModules)
    }

    private fun createShader(shaderCode: String, shaderType: ShaderType): Int {
        val shaderId = glCreateShader(shaderType.id)
        if (shaderId == 0) {
            throw RuntimeException("Error creating shader type: $shaderType")
        }

        glShaderSource(shaderId, shaderCode)
        glCompileShader(shaderId)

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw RuntimeException("Error compiling shader: ${glGetShaderInfoLog(shaderId, 1024)}")
        }

        glAttachShader(programId, shaderId)
        return shaderId
    }

    private fun link(shaderModules: List<Int>) {
        glLinkProgram(programId)
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw RuntimeException("Error linking shader: ${glGetProgramInfoLog(programId, 1024)}")
        }

        shaderModules.forEach { shaderModule ->
            glDetachShader(programId, shaderModule)
        }
        shaderModules.forEach { shaderModule ->
            glDeleteShader(shaderModule)
        }
    }

    fun bind() {
        glUseProgram(programId)
    }

    fun unbind() {
        glUseProgram(0)
    }

    fun cleanup() {
        unbind()
        if (programId != 0) {
            glDeleteProgram(programId)
        }
    }

    fun validate() {
        glValidateProgram(programId)
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            throw RuntimeException("Error validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }
}
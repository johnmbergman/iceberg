package com.johnmbergman.iceberg.graphics

import org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER

enum class ShaderType(val id: Int) {
    VERTEX(GL_VERTEX_SHADER),
    FRAGMENT(GL_FRAGMENT_SHADER),
}
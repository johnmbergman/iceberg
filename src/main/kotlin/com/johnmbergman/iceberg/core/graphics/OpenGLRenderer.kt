package com.johnmbergman.iceberg.core.graphics

import com.johnmbergman.iceberg.Window
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT
import org.lwjgl.opengl.GL11C.glClear

class OpenGLRenderer: Renderer {

    constructor() {
        GL.createCapabilities()
    }

    override fun cleanup() {}

    override fun render(window: Window) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
    }
}
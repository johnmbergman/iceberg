package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.Window
import com.johnmbergman.iceberg.scene.Scene
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.GL_BACK
import org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT
import org.lwjgl.opengl.GL11C.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11C.glClear
import org.lwjgl.opengl.GL11C.glCullFace
import org.lwjgl.opengl.GL11C.glEnable
import org.lwjgl.opengl.GL11C.glViewport

class OpenGLRenderer: Renderer {

    private val sceneRenderer: SceneRenderer

    constructor() {
        GL.createCapabilities()
        glEnable(GL_DEPTH_TEST)
        glCullFace(GL_BACK)
        sceneRenderer = SceneRenderer()
    }

    override fun cleanup() {
        sceneRenderer.cleanup()
    }

    override fun render(window: Window, scene: Scene) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glViewport(0, 0, window.width, window.height)
        sceneRenderer.render(scene)
    }
}
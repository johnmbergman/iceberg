package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.Window
import com.johnmbergman.iceberg.scene.Scene
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
    private val uiRenderer: UIRenderer
    private val skyBoxRenderer: SkyBoxRenderer

    /** Requires the window's OpenGL context to be current. */
    constructor(textureCache: TextureCache) {
        glEnable(GL_DEPTH_TEST)
        glCullFace(GL_BACK)
        sceneRenderer = SceneRenderer(textureCache)
        uiRenderer = UIRenderer()
        skyBoxRenderer = SkyBoxRenderer(textureCache)
    }

    override fun cleanup() {
        sceneRenderer.cleanup()
        uiRenderer.cleanup()
        skyBoxRenderer.cleanup()
    }

    override fun render(window: Window, scene: Scene) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glViewport(0, 0, window.width, window.height)
        skyBoxRenderer.render(scene)
        sceneRenderer.render(scene)
        uiRenderer.render(window)
    }
}
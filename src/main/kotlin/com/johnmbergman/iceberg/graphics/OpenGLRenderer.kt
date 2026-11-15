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
    private val shadowRenderer: ShadowRenderer
    private val resources: ResourceCache

    // Rebuilt every frame and shared by the shadow and scene passes; kept so its lists are reused
    private val renderQueue = RenderQueue()

    /** Requires the window's OpenGL context to be current. */
    constructor(resources: ResourceCache) {
        this.resources = resources
        glEnable(GL_DEPTH_TEST)
        glCullFace(GL_BACK)
        sceneRenderer = SceneRenderer(resources)
        uiRenderer = UIRenderer()
        skyBoxRenderer = SkyBoxRenderer(resources)
        shadowRenderer = ShadowRenderer()
    }

    override fun cleanup() {
        sceneRenderer.cleanup()
        uiRenderer.cleanup()
        skyBoxRenderer.cleanup()
        shadowRenderer.cleanup()
    }

    override fun render(window: Window, scene: Scene) {
        renderQueue.build(scene)
        // The shadow pass draws into its own framebuffer at the shadow map size, so it goes first and the
        // viewport is set back to the window afterwards
        shadowRenderer.render(scene, renderQueue, resources)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glViewport(0, 0, window.width, window.height)
        skyBoxRenderer.render(scene)
        sceneRenderer.render(scene, renderQueue, shadowRenderer)
        uiRenderer.render(window)
    }
}
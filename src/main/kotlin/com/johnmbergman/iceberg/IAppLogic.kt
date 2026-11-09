package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.ResourceCache
import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.ui.UserInterface

interface IAppLogic {
    fun cleanup() { }
    fun onInit(window: Window, scene: Scene, renderer: Renderer, ui: UserInterface, resources: ResourceCache) { }
    fun onInput(window: Window, scene: Scene, deltaMillis: Long) { }
    fun onUpdate(window: Window, scene: Scene, deltaMillis: Long) { }
    /** Build this frame's UI with ImGui widget calls; the engine begins and ends the ImGui frame around it. */
    fun onDrawUI(window: Window, scene: Scene) { }
}
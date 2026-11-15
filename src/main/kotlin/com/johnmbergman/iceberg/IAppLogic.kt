package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.ResourceCache
import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.ui.UserInterface
import kotlin.time.Duration

interface IAppLogic {
    fun cleanup() { }
    fun onInit(window: Window, scene: Scene, renderer: Renderer, ui: UserInterface, resources: ResourceCache) { }
    /** Called once per frame, before [onUpdate]; [delta] is the time since the previous frame. */
    fun onInput(window: Window, scene: Scene, delta: Duration) { }
    /** Called once per frame; [delta] is the time since the previous frame, so scale movement by it. */
    fun onUpdate(window: Window, scene: Scene, delta: Duration) { }
    /** Build this frame's UI with ImGui widget calls; the engine begins and ends the ImGui frame around it. */
    fun onDrawUI(window: Window, scene: Scene) { }
}
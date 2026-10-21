package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.Renderer
import com.johnmbergman.iceberg.scene.Scene

interface IAppLogic {
    fun cleanup() { }
    fun onInit(window: Window, scene: Scene, renderer: Renderer) { }
    fun onInput(window: Window, scene: Scene, deltaMillis: Long) { }
    fun onUpdate(window: Window, scene: Scene, deltaMillis: Long) { }
}
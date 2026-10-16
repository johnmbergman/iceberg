package com.johnmbergman.iceberg.graphics

import com.johnmbergman.iceberg.Window
import com.johnmbergman.iceberg.scene.Scene

interface Renderer {
    fun render(window: Window, scene: Scene)
    fun cleanup()
}
package com.johnmbergman.iceberg.core.graphics

import com.johnmbergman.iceberg.Window

interface Renderer {
    fun render(window: Window)
    fun cleanup()
}
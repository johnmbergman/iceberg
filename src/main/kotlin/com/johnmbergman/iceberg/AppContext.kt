package com.johnmbergman.iceberg

import com.johnmbergman.iceberg.graphics.ResourceCache
import com.johnmbergman.iceberg.scene.Scene
import com.johnmbergman.iceberg.ui.UserInterface

/**
 * The engine services an [IAppLogic] works with, passed to every hook. These live as long as the engine; per-frame
 * values such as the frame delta are passed to the hooks separately.
 */
class AppContext internal constructor(
    val window: Window,
    val ui: UserInterface,
    val resources: ResourceCache,
    scene: Scene,
) {
    /** The scene being updated and rendered. Read it from here each time rather than keeping a reference to it. */
    var scene: Scene = scene
        internal set
}

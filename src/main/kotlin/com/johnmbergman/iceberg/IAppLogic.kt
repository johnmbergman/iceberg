package com.johnmbergman.iceberg

import kotlin.time.Duration

interface IAppLogic {
    fun cleanup() { }
    fun onInit(context: AppContext) { }
    /** Called once per frame, before [onUpdate]; [delta] is the time since the previous frame. */
    fun onInput(context: AppContext, delta: Duration) { }
    /** Called once per frame; [delta] is the time since the previous frame, so scale movement by it. */
    fun onUpdate(context: AppContext, delta: Duration) { }
    /** Build this frame's UI with ImGui widget calls; the engine begins and ends the ImGui frame around it. */
    fun onDrawUI(context: AppContext) { }
}

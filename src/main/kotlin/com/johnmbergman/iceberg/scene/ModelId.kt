package com.johnmbergman.iceberg.scene

/**
 * Identifies a model loaded by [com.johnmbergman.iceberg.graphics.ResourceCache]; get one from its `loadModel`.
 * Scene code stores ids, never GPU objects; renderers look the model up when drawing.
 */
@JvmInline
value class ModelId(val path: String)

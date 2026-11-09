package com.johnmbergman.iceberg.assets

/** A model as read from a file, before anything is uploaded to the GPU. */
class ModelData(val id: String, val materials: List<MaterialData>)

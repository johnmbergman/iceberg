package com.johnmbergman.iceberg.assets

/**
 * Vertex data for one mesh, ready to upload to the GPU. Every per-vertex array has one entry per vertex:
 * three floats for positions, normals, tangents and bitangents, two for texture coordinates.
 *
 * Not a data class: data classes compare arrays by reference, so the generated equals() would mislead.
 */
class MeshData(
    val positions: FloatArray,
    val normals: FloatArray,
    val tangents: FloatArray,
    val bitangents: FloatArray,
    val textureCoordinates: FloatArray,
    val indices: IntArray,
) {
    val vertexCount: Int
        get() = positions.size / 3
}

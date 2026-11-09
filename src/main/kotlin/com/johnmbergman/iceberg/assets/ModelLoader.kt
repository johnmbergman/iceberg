package com.johnmbergman.iceberg.assets

import com.johnmbergman.iceberg.math.Color
import org.lwjgl.assimp.AIColor4D
import org.lwjgl.assimp.AIMaterial
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.AIString
import org.lwjgl.assimp.AIVector3D
import org.lwjgl.assimp.Assimp.*
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.IntBuffer

/**
 * Reads model files with Assimp into plain [ModelData]. Makes no OpenGL calls, so it works without a GL context;
 * [com.johnmbergman.iceberg.graphics.ModelUploader] turns the result into GPU resources.
 */
object ModelLoader {

    const val DEFAULT_FLAGS = aiProcess_GenSmoothNormals or
            aiProcess_JoinIdenticalVertices or
            aiProcess_Triangulate or
            aiProcess_FixInfacingNormals or
            aiProcess_CalcTangentSpace or
            aiProcess_LimitBoneWeights or
            aiProcess_PreTransformVertices

    /**
     * Loads a model from a classpath resource. Assimp needs a real file (to resolve .mtl and texture
     * siblings), so this only works while resources are unpacked on disk, not from inside a jar.
     */
    fun load(modelId: String, modelPath: String, flags: Int = DEFAULT_FLAGS): ModelData {
        val resourcePath = modelPath.removePrefix("/")
        val url = ModelLoader::class.java.getResource("/$resourcePath")
            ?: throw RuntimeException("Model not found on classpath: $resourcePath")
        val file = File(url.toURI())
        // Classpath directory, so texture paths resolve as classpath resources too
        val modelDirectory = resourcePath.substringBeforeLast('/', "")

        val aiScene = aiImportFile(file.absolutePath, flags)
            ?: throw RuntimeException("Failed to load model: $modelPath: ${aiGetErrorString()}")
        // The imported scene lives in native memory and must be released explicitly
        try {
            val properties = (0 until aiScene.mNumMaterials()).map { i ->
                readMaterial(AIMaterial.create(aiScene.mMaterials()!!.get(i)), modelDirectory)
            }

            val meshesByMaterial = List(properties.size) { mutableListOf<MeshData>() }
            val meshesWithoutMaterial = mutableListOf<MeshData>()
            val aiMeshes = aiScene.mMeshes()!!
            for (i in 0 until aiScene.mNumMeshes()) {
                val aiMesh = AIMesh.create(aiMeshes.get(i))
                val target = meshesByMaterial.getOrNull(aiMesh.mMaterialIndex()) ?: meshesWithoutMaterial
                target.add(readMesh(aiMesh))
            }

            val materials = properties.mapIndexed { i, material -> material.toMaterialData(meshesByMaterial[i]) }
            // Meshes with an invalid material index get a material with default values
            val fallback = if (meshesWithoutMaterial.isEmpty()) emptyList() else listOf(MaterialData(meshes = meshesWithoutMaterial))
            return ModelData(modelId, materials + fallback)
        } finally {
            aiReleaseImport(aiScene)
        }
    }

    /**
     * Turns a texture path from a model file into a classpath resource path. Model files often contain absolute
     * or tool-specific paths, so only the file name is kept and looked up next to the model.
     */
    internal fun resolveTexturePath(modelDirectory: String, rawPath: String): String {
        // Split on both separators: paths written on Windows use backslashes, which java.io.File ignores elsewhere
        val fileName = rawPath.substringAfterLast('/').substringAfterLast('\\')
        return listOf(modelDirectory, fileName).filter { it.isNotEmpty() }.joinToString("/")
    }

    /** Everything about a material except its meshes, which are read afterwards. */
    private class MaterialProperties(
        val diffuseTexturePath: String?,
        val normalMapPath: String?,
        val ambientColor: Color?,
        val diffuseColor: Color?,
        val specularColor: Color?,
        val reflectance: Float?,
    ) {
        fun toMaterialData(meshes: List<MeshData>) = MaterialData(
            diffuseTexturePath = diffuseTexturePath,
            normalMapPath = normalMapPath,
            ambientColor = ambientColor ?: MaterialData.DEFAULT_COLOR,
            diffuseColor = diffuseColor ?: MaterialData.DEFAULT_COLOR,
            specularColor = specularColor ?: MaterialData.DEFAULT_COLOR,
            reflectance = reflectance ?: 0f,
            meshes = meshes,
        )
    }

    private fun readMaterial(aiMaterial: AIMaterial, modelDirectory: String): MaterialProperties {
        MemoryStack.stackPush().use { stack ->
            fun readTexturePath(type: Int): String? {
                val path = AIString.calloc(stack)
                aiGetMaterialTexture(aiMaterial, type, 0, path, null as IntBuffer?, null, null, null, null, null)
                val rawPath = path.dataString()
                return if (rawPath.isEmpty()) null else resolveTexturePath(modelDirectory, rawPath)
            }

            val color = AIColor4D.calloc(stack)
            fun readColor(key: String): Color? =
                if (aiGetMaterialColor(aiMaterial, key, aiTextureType_NONE, 0, color) == aiReturn_SUCCESS) {
                    Color(color.r(), color.g(), color.b(), color.a())
                } else {
                    null
                }

            val diffuseTexturePath = readTexturePath(aiTextureType_DIFFUSE)

            val shininessStrength = floatArrayOf(0f)
            val max = intArrayOf(1)
            val reflectance = if (
                aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS_STRENGTH, aiTextureType_NONE, 0, shininessStrength, max)
                == aiReturn_SUCCESS
            ) shininessStrength[0] else null

            return MaterialProperties(
                diffuseTexturePath = diffuseTexturePath,
                normalMapPath = readTexturePath(aiTextureType_NORMALS),
                ambientColor = readColor(AI_MATKEY_COLOR_AMBIENT),
                // A textured material takes its diffuse color from the texture, so keep the default there
                diffuseColor = if (diffuseTexturePath == null) readColor(AI_MATKEY_COLOR_DIFFUSE) else null,
                specularColor = readColor(AI_MATKEY_COLOR_SPECULAR),
                reflectance = reflectance,
            )
        }
    }

    private fun readMesh(aiMesh: AIMesh): MeshData {
        val vertexCount = aiMesh.mNumVertices()
        // Normals, tangents and bitangents can be missing (tangents need texture coordinates), and the shader
        // still expects every attribute, so missing ones are filled with zeros
        return MeshData(
            positions = readVectors(aiMesh.mVertices()),
            normals = aiMesh.mNormals()?.let(::readVectors) ?: FloatArray(vertexCount * 3),
            tangents = aiMesh.mTangents()?.let(::readVectors) ?: FloatArray(vertexCount * 3),
            bitangents = aiMesh.mBitangents()?.let(::readVectors) ?: FloatArray(vertexCount * 3),
            textureCoordinates = readTextureCoordinates(aiMesh) ?: FloatArray(vertexCount * 2),
            indices = readIndices(aiMesh),
        )
    }

    private fun readVectors(buffer: AIVector3D.Buffer): FloatArray {
        val data = FloatArray(buffer.remaining() * 3)
        var pos = 0
        while (buffer.remaining() > 0) {
            val vector = buffer.get()
            data[pos++] = vector.x()
            data[pos++] = vector.y()
            data[pos++] = vector.z()
        }
        return data
    }

    private fun readTextureCoordinates(aiMesh: AIMesh): FloatArray? {
        val buffer = aiMesh.mTextureCoords(0) ?: return null
        val data = FloatArray(buffer.remaining() * 2)
        var pos = 0
        while (buffer.remaining() > 0) {
            val coordinate = buffer.get()
            data[pos++] = coordinate.x()
            // Image rows start at the top, while texture V starts at the bottom
            data[pos++] = 1 - coordinate.y()
        }
        return data
    }

    private fun readIndices(aiMesh: AIMesh): IntArray {
        val indices = mutableListOf<Int>()
        val aiFaces = aiMesh.mFaces()
        for (i in 0 until aiMesh.mNumFaces()) {
            val buffer = aiFaces.get(i).mIndices()
            while (buffer.remaining() > 0) {
                indices.add(buffer.get())
            }
        }
        return indices.toIntArray()
    }
}

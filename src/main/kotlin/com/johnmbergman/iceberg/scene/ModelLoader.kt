package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.graphics.Material
import com.johnmbergman.iceberg.graphics.Mesh
import com.johnmbergman.iceberg.graphics.Model
import com.johnmbergman.iceberg.graphics.TextureCache
import org.joml.Vector4f
import org.lwjgl.assimp.AIColor4D
import org.lwjgl.assimp.AIMaterial
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.AIString
import org.lwjgl.assimp.Assimp.*
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.IntBuffer


object ModelLoader {

    const val DEFAULT_FLAGS = aiProcess_GenSmoothNormals or
            aiProcess_JoinIdenticalVertices or
            aiProcess_Triangulate or
            aiProcess_FixInfacingNormals or
            aiProcess_CalcTangentSpace or
            aiProcess_LimitBoneWeights or
            aiProcess_PreTransformVertices

    fun loadModel(modelId: String, modelPath: String, textureCache: TextureCache): Model {
        return loadModel(modelId, modelPath, textureCache, DEFAULT_FLAGS);
    }

    /**
     * Loads a model from a classpath resource. Assimp needs a real file (to resolve .mtl and texture
     * siblings), so this only works while resources are unpacked on disk, not from inside a jar.
     */
    fun loadModel(modelId: String, modelPath: String, textureCache: TextureCache, flags: Int): Model {
        val resourcePath = modelPath.removePrefix("/")
        val url = ModelLoader::class.java.getResource("/$resourcePath")
            ?: throw RuntimeException("Model not found on classpath: $resourcePath")
        val file = File(url.toURI())
        // Classpath directory, so textures resolve through TextureCache the same way
        val modelDirectory = resourcePath.substringBeforeLast('/', "")

        val aiScene = aiImportFile(file.absolutePath, flags)
            ?: throw RuntimeException("Failed to load model: $modelPath")

        val numberOfMaterials = aiScene.mNumMaterials()
        val materialList = mutableListOf<Material>()
        for (i in 0 until numberOfMaterials) {
            val aiMaterial = AIMaterial.create(aiScene.mMaterials()!!.get(i))
            materialList.add(processMaterial(aiMaterial, modelDirectory, textureCache))
        }

        val numberOfMeshes = aiScene.mNumMeshes()
        val aiMeshes = aiScene.mMeshes()!!
        val defaultMaterial = Material(textureCache.default)
        for (i in 0 until numberOfMeshes) {
            val aiMesh = AIMesh.create(aiMeshes.get(i))
            val mesh = processMesh(aiMesh)
            val materialIndex = aiMesh.mMaterialIndex()
            val material = if (materialIndex >= 0 && materialIndex < materialList.size) {
                materialList[materialIndex]
            } else {
                defaultMaterial
            }
            material.meshList.add(mesh)
        }

        if (defaultMaterial.meshList.isNotEmpty()) {
            materialList.add(defaultMaterial)
        }

        return Model(modelId, materialList)
    }

    private fun processMaterial(aiMaterial: AIMaterial, modelDirectory: String, textureCache: TextureCache): Material {
        MemoryStack.stackPush().use { stack ->
            val aiTexturePath = AIString.calloc(stack)
            aiGetMaterialTexture(
                aiMaterial, aiTextureType_DIFFUSE, 0, aiTexturePath, null as IntBuffer?,
                null, null, null, null, null
            )
            val texturePath = aiTexturePath.dataString()
            if (texturePath.isNotEmpty()) {
                return Material(textureCache[listOf(modelDirectory, File(texturePath).name).filter { it.isNotEmpty() }.joinToString("/")])
            }

            val material = Material(textureCache.default)
            val color = AIColor4D.calloc(stack)
            val result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color)
            if (result == aiReturn_SUCCESS) {
                material.diffuseColor = Vector4f(color.r(), color.g(), color.b(), color.a())
            }
            return material
        }
    }

    private fun processMesh(aiMesh: AIMesh): Mesh {
        val vertices = processVertices(aiMesh)
        var textureCoordinates = processTextureCoordinates(aiMesh)
        val indices = processIndices(aiMesh)

        // Texture coordinates may not have been populated. We need at least the empty slots
        if (textureCoordinates.isEmpty()) {
            val numElements: Int = (vertices.size / 3) * 2
            textureCoordinates = FloatArray(numElements)
        }

        return Mesh(vertices, textureCoordinates, indices)
    }

    private fun processIndices(aiMesh: AIMesh): IntArray {
        val indices = mutableListOf<Int>()
        val numFaces = aiMesh.mNumFaces()
        val aiFaces = aiMesh.mFaces()
        for (i in 0..<numFaces) {
            val aiFace = aiFaces.get(i)
            val buffer = aiFace.mIndices()
            while (buffer.remaining() > 0) {
                indices.add(buffer.get())
            }
        }
        return indices.toIntArray()
    }

    private fun processTextureCoordinates(aiMesh: AIMesh): FloatArray {
        val buffer = aiMesh.mTextureCoords(0) ?: return floatArrayOf()
        val data = FloatArray(buffer.remaining() * 2)
        var pos = 0
        while (buffer.remaining() > 0) {
            val textCoord = buffer.get()
            data[pos++] = textCoord.x()
            data[pos++] = 1 - textCoord.y()
        }
        return data
    }

    private fun processVertices(aiMesh: AIMesh): FloatArray {
        val buffer = aiMesh.mVertices()
        val data = FloatArray(buffer.remaining() * 3)
        var pos = 0
        while (buffer.remaining() > 0) {
            val textCoord = buffer.get()
            data[pos++] = textCoord.x()
            data[pos++] = textCoord.y()
            data[pos++] = textCoord.z()
        }
        return data
    }
}
package com.johnmbergman.iceberg.scene

import com.johnmbergman.iceberg.TOLERANCE
import com.johnmbergman.iceberg.assertVectorEquals
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertTrue

class EntityTest {

    @Test
    fun startsWithIdentityModelMatrix() {
        val entity = Entity("entity", "model")
        assertTrue(Matrix4f(entity.modelMatrix).equals(Matrix4f(), TOLERANCE))
    }

    @Test
    fun positionTranslatesTheModel() {
        val entity = Entity("entity", "model")
        entity.setPosition(1f, 2f, 3f)
        entity.updateModelMatrix()

        assertVectorEquals(1f, 2f, 3f, entity.modelMatrix.transformPosition(Vector3f()))
    }

    @Test
    fun appliesScaleThenRotationThenTranslation() {
        val entity = Entity("entity", "model")
        entity.scale = 2f
        entity.setRotation(0f, 1f, 0f, (PI / 2).toFloat()) // A quarter turn about +Y maps +X to -Z
        entity.setPosition(10f, 0f, 0f)
        entity.updateModelMatrix()

        // (1,0,0) → scaled (2,0,0) → rotated (0,0,-2) → translated (10,0,-2)
        assertVectorEquals(10f, 0f, -2f, entity.modelMatrix.transformPosition(Vector3f(1f, 0f, 0f)))
    }

    /** Pins the current manual-update behavior; item 4 (dirty-flag transforms) will replace this test. */
    @Test
    fun modelMatrixIsStaleUntilUpdateModelMatrixIsCalled() {
        val entity = Entity("entity", "model")
        entity.setPosition(1f, 2f, 3f)

        assertTrue(Matrix4f(entity.modelMatrix).equals(Matrix4f(), TOLERANCE))
        entity.updateModelMatrix()
        assertVectorEquals(1f, 2f, 3f, entity.modelMatrix.transformPosition(Vector3f()))
    }
}

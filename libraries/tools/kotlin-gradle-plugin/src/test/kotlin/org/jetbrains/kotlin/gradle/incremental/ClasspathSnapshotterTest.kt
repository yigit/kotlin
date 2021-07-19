/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("SpellCheckingInspection")

package org.jetbrains.kotlin.gradle.incremental

import org.jetbrains.kotlin.gradle.incremental.ClasspathSnapshotTestCommon.Companion.SourceFile
import org.jetbrains.kotlin.gradle.incremental.ClasspathSnapshotTestCommon.Util.snapshot
import org.jetbrains.kotlin.gradle.incremental.ClasspathSnapshotTestCommon.Util.toGson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.io.File

abstract class ClasspathSnapshotterTest : ClasspathSnapshotTestCommon() {

    protected abstract val testSourceFile: SourceFile

    private lateinit var testClassSnapshot: ClassSnapshot

    @Before
    fun setUp() {
        testClassSnapshot = testSourceFile.compile().snapshot()
    }

    @Test
    fun `test ClassSnapshotter's result against expected snapshot`() {
        val expectedSnapshot =
            File(testDataDir, "${testSourceFile.relativePath.substringBeforeLast('.')}-expected-snapshot.json").readText()
        assertEquals(expectedSnapshot, testClassSnapshot.toGson())
    }

    @Test
    fun `test ClassSnapshotter extracts ABI info from a class`() {
        // Change public method signature
        val updatedSnapshot = testSourceFile
            .replace("fun publicMethod()", "fun changedPublicMethod()")
            .compile()
            .snapshot()

        // The snapshot must change
        assertNotEquals(testClassSnapshot.toGson(), updatedSnapshot.toGson())
    }

    @Test
    fun `test ClassSnapshotter does not extract non-ABI info from a class`() {
        // Change method implementation
        val updatedSnapshot = testSourceFile
            .replace("I'm in a public method", "This method implementation has changed")
            .compile()
            .snapshot()

        // The snapshot must not change
        assertEquals(testClassSnapshot.toGson(), updatedSnapshot.toGson())
    }
}

class KotlinClassesClasspathSnapshotterTest : ClasspathSnapshotterTest() {

    override val testSourceFile = SourceFile(testDataDir, "com/example/SimpleKotlinClass.kt")
}

/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.incremental

import org.jetbrains.kotlin.gradle.incremental.ClasspathSnapshotTestCommon.Companion.SourceFile
import org.jetbrains.kotlin.gradle.incremental.ClasspathSnapshotTestCommon.Util.snapshot
import org.jetbrains.kotlin.incremental.DirtyData
import org.jetbrains.kotlin.incremental.LookupSymbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.sam.SAM_LOOKUP_NAME
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

abstract class ClasspathChangesComputerTest : ClasspathSnapshotTestCommon() {

    protected abstract val testSourceFile: SourceFile

    private lateinit var originalSnapshot: ClassSnapshot

    @Before
    fun setUp() {
        originalSnapshot = testSourceFile.compile().snapshot()
    }

    // TODO Add more test cases:
    //   - private/non-private fields
    //   - inline functions
    //   - changing supertype by adding somethings that changes/does not change the supertype ABI
    //   - adding an annotation

    @Test
    fun testCollectClassChanges_changedPublicMethodSignature() {
        val updatedSnapshot = testSourceFile.replace("fun publicMethod()", "fun changedPublicMethod()")
            .compile()
            .snapshot()
        val dirtyData = ClasspathChangesComputer.collectKotlinClassChanges(
            updatedSnapshot as KotlinClassSnapshot,
            originalSnapshot as KotlinClassSnapshot
        )

        assertEquals(
            DirtyData(
                dirtyLookupSymbols = setOf(
                    LookupSymbol(name = SAM_LOOKUP_NAME.asString(), scope = "com.example.SimpleKotlinClass"),
                    LookupSymbol(name = "publicMethod", scope = "com.example.SimpleKotlinClass"),
                    LookupSymbol(name = "changedPublicMethod", scope = "com.example.SimpleKotlinClass")
                ),
                dirtyClassesFqNames = setOf(FqName("com.example.SimpleKotlinClass")),
                dirtyClassesFqNamesForceRecompile = emptySet()
            ),
            dirtyData
        )
    }

    @Test
    fun testCollectClassChanges_changedMethodImplementation() {
        val updatedSnapshot = testSourceFile
            .replace("I'm in a public method", "This method implementation has changed")
            .compile()
            .snapshot()
        val dirtyData = ClasspathChangesComputer.collectKotlinClassChanges(
            updatedSnapshot as KotlinClassSnapshot,
            originalSnapshot as KotlinClassSnapshot
        )

        assertEquals(DirtyData(emptySet(), emptySet(), emptySet()), dirtyData)
    }
}

class KotlinClassesClasspathChangesComputerTest : ClasspathChangesComputerTest() {

    override val testSourceFile = Companion.SourceFile(testDataDir, "com/example/SimpleKotlinClass.kt")
}

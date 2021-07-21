/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.incremental

import com.google.gson.GsonBuilder
import org.jetbrains.kotlin.test.MockLibraryUtil.compileKotlin
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

abstract class ClasspathSnapshotTestCommon {

    companion object {

        val testDataDir = File("libraries/tools/kotlin-gradle-plugin/src/testData/kotlin.incremental.useClasspathSnapshot/src")

        data class SourceFile(val baseDir: File, val relativePath: String) {

            fun asFile() = File(baseDir, relativePath)
        }
    }

    @get:Rule
    val tmp = TemporaryFolder()

    protected fun SourceFile.compile(): File {
        val classesDir = tmp.newFolder()

        compileKotlin(asFile().path, classesDir)

        return File(classesDir, "${relativePath.substringBeforeLast(".")}.class")
    }

    protected fun SourceFile.replace(oldValue: String, newValue: String): SourceFile {
        val newSourceFile = SourceFile(tmp.newFolder(), this.relativePath).also { it.asFile().parentFile.mkdirs() }
        newSourceFile.asFile().writeText(this.asFile().readText().replace(oldValue, newValue))
        return newSourceFile
    }

    object Util {

        fun File.snapshot() = ClassSnapshotter.snapshot(readBytes())

        // Use Gson to compare objects
        private val gson by lazy { GsonBuilder().setPrettyPrinting().create() }
        fun Any.toGson(): String = gson.toJson(this)
    }
}

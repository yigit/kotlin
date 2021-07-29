/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.utils.filesProvider

/**
 * Reorder the compiler plugin artifacts as resolved by the [fromConfiguration], producing a [FileCollection] with the artifacts
 * order so that files from modules under [prioritizedPluginArtifactCoordinates] appear first.
 *
 * In particular, the serialization plugin needs to appear first on the -Xplugin classpath in order to avoid conflicts with other compiler
 * plugins producing unexpected IR.
 *
 * KT-47921
 */
internal fun orderedCompilePluginClasspath(project: Project, fromConfiguration: Configuration): FileCollection =
    project.filesProvider {
        fromConfiguration.incoming.artifacts.artifacts
            .sortedBy { it.file.absolutePath }
            .partition {
                val id = it.id.componentIdentifier
                id is ModuleComponentIdentifier && (id.group to id.module) in prioritizedPluginArtifactCoordinates
            }
            .run { first + second }
            .map { it.file }
    }

private val prioritizedPluginArtifactCoordinates = setOf(
    "org.jetbrains.kotlin" to "kotlin-serialization",
    "org.jetbrains.kotlin" to "kotlin-serialization-unshaded"
)
/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.jvm.checkers

import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.descriptors.annotations.KotlinRetention
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.resolve.AdditionalAnnotationChecker
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.getAnnotationRetention
import org.jetbrains.kotlin.resolve.descriptorUtil.isAnnotatedWithKotlinRepeatable
import org.jetbrains.kotlin.resolve.jvm.JvmPlatformAnnotationFeaturesSupport
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm

class RepeatableAnnotationChecker(
    languageVersionSettings: LanguageVersionSettings,
    private val jvmTarget: JvmTarget,
    private val platformAnnotationFeaturesSupport: JvmPlatformAnnotationFeaturesSupport,
) : AdditionalAnnotationChecker {
    private val nonSourceDisallowed = !languageVersionSettings.supportsFeature(LanguageFeature.RepeatableAnnotations)

    override fun checkEntries(
        entries: List<KtAnnotationEntry>,
        actualTargets: List<KotlinTarget>,
        trace: BindingTrace,
        languageVersionSettings: LanguageVersionSettings
    ) {
        if (entries.isEmpty()) return

        val entryTypesWithAnnotations = hashMapOf<FqName, MutableList<AnnotationUseSiteTarget?>>()

        for (entry in entries) {
            val descriptor = trace.get(BindingContext.ANNOTATION, entry) ?: continue
            val fqName = descriptor.fqName ?: continue
            val classDescriptor = descriptor.annotationClass ?: continue

            val useSiteTarget = entry.useSiteTarget?.getAnnotationUseSiteTarget()
            val existingTargetsForAnnotation = entryTypesWithAnnotations.getOrPut(fqName) { arrayListOf() }
            val duplicateAnnotation = useSiteTarget in existingTargetsForAnnotation
                    || (existingTargetsForAnnotation.any { (it == null) != (useSiteTarget == null) })

            if (duplicateAnnotation
                && isRepeatableAnnotation(classDescriptor)
                && classDescriptor.getAnnotationRetention() != KotlinRetention.SOURCE
            ) {
                val error = when {
                    jvmTarget == JvmTarget.JVM_1_6 -> ErrorsJvm.REPEATED_ANNOTATION_TARGET6
                    nonSourceDisallowed -> ErrorsJvm.NON_SOURCE_REPEATED_ANNOTATION
                    else -> null
                }
                if (error != null) {
                    trace.report(error.on(entry))
                }
            }

            existingTargetsForAnnotation.add(useSiteTarget)
        }
    }

    private fun isRepeatableAnnotation(classDescriptor: ClassDescriptor): Boolean =
        classDescriptor.isAnnotatedWithKotlinRepeatable() || platformAnnotationFeaturesSupport.isRepeatableAnnotationClass(classDescriptor)
}

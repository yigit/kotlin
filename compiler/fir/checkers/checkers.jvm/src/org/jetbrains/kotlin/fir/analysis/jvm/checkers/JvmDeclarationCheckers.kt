/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.jvm.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.jvm.checkers.declaration.FirJvmExternalDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.jvm.checkers.declaration.FirJvmNameChecker
import org.jetbrains.kotlin.fir.analysis.jvm.checkers.declaration.FirJvmStaticChecker

object JvmDeclarationCheckers : DeclarationCheckers() {
    override val basicDeclarationCheckers: Set<FirBasicDeclarationChecker>
        get() = setOf(
            FirJvmExternalDeclarationChecker,
            FirJvmNameChecker,
            FirJvmStaticChecker,
        )
}

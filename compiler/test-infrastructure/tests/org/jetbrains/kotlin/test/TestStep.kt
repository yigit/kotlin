/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

import org.jetbrains.kotlin.test.TestRunner.Companion.shouldRun
import org.jetbrains.kotlin.test.model.*

sealed class TestStep<I : ResultingArtifact<I>> {
    abstract val inputArtifactKind: TestArtifactKind<I>

    protected abstract fun shouldProcessModule(module: TestModule): Boolean

    fun shouldProcessArtifact(module: TestModule, inputArtifact: ResultingArtifact<*>): Boolean {
        return inputArtifact.kind == inputArtifactKind && shouldProcessModule(module)
    }

    class FacadeStep<I : ResultingArtifact<I>, O : ResultingArtifact<O>>(val facade: AbstractTestFacade<I, O>) : TestStep<I>() {
        override val inputArtifactKind: TestArtifactKind<I>
            get() = facade.inputKind

        val outputArtifactKind: TestArtifactKind<O>
            get() = facade.outputKind

        override fun shouldProcessModule(module: TestModule): Boolean {
            return facade.shouldRunAnalysis(module)
        }

        fun processModule(module: TestModule, inputArtifact: I): StepResult<O> {
            val outputArtifact = try {
                facade.transform(module, inputArtifact) ?: return StepResult.NoArtifactFromFacade
            } catch (e: Throwable) {
                // TODO: remove inheritors of WrappedException.FromFacade
                return StepResult.ErrorFromFacade(WrappedException.FromFacade.Frontend(e))
            }
            return StepResult.Artifact(outputArtifact)
        }
    }

    class HandlersStep<I : ResultingArtifact<I>>(
        override val inputArtifactKind: TestArtifactKind<I>,
        val handlers: List<AnalysisHandler<I>>
    ) : TestStep<I>() {
        init {
            require(handlers.all { it.artifactKind == inputArtifactKind })
        }

        override fun shouldProcessModule(module: TestModule): Boolean {
            return true
        }

        fun checkArtifact(module: TestModule, artifact: I, thereWereExceptionsOnPreviousSteps: Boolean): StepResult.HandlersResult {
            val exceptions = mutableListOf<WrappedException>()
            for (outputHandler in handlers) {
                if (outputHandler.shouldRun(thereWasAnException = thereWereExceptionsOnPreviousSteps || exceptions.isNotEmpty())) {
                    try {
                        outputHandler.processModule(module, artifact)
                    } catch (e: Throwable) {
                        // TODO
                        exceptions += WrappedException.FromFrontendHandler(e)
                        if (outputHandler.failureDisablesNextSteps) {
                            return StepResult.HandlersResult(exceptions, shouldRunNextSteps = false)
                        }
                    }
                }
            }
            return StepResult.HandlersResult(exceptions, shouldRunNextSteps = true)
        }
    }

    sealed class StepResult<out O : ResultingArtifact<out O>> {
        class Artifact<out O : ResultingArtifact<out O>>(val outputArtifact: O, ) : StepResult<O>()
        class ErrorFromFacade<O : ResultingArtifact<O>>(val exception: WrappedException) : StepResult<O>()
        data class HandlersResult(
            val exceptionsFromHandlers: Collection<WrappedException>,
            val shouldRunNextSteps: Boolean
        ) : StepResult<Nothing>()

        object NoArtifactFromFacade : StepResult<Nothing>()
    }
}



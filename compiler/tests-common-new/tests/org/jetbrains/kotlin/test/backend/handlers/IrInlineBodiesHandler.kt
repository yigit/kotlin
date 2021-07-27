/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.handlers

class IrInlineBodiesHandler(testServices: TestServices) : AbstractIrHandler(testServices) {
    val declaredInlineFunctionSignatures = mutableSetOf<IdSignature>()

    override fun processModule(module: TestModule, info: IrBackendInput) {
        val irModule = info.backendInput.irModuleFragment
        irModule.acceptChildrenVoid(InlineFunctionsCollector())
        irModule.acceptChildrenVoid(InlineCallBodiesCheck())
        assertions.assertTrue(info.backendInput.symbolTable.allUnbound.isEmpty())
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        // TODO("Not yet implemented")
    }

    inner class InlineFunctionsCollector : IrElementVisitorVoid {
        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitSimpleFunction(declaration: IrSimpleFunction) {
            if (declaration.isInline) declaration.symbol.signature?.let { declaredInlineFunctionSignatures.add(it) }
            super.visitSimpleFunction(declaration)
        }
    }

    inner class InlineCallBodiesCheck : IrElementVisitorVoid {
        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) {
            val symbol = expression.symbol
            assertions.assertTrue(symbol.isBound)
            val callee = symbol.owner
            if (callee.symbol.signature in declaredInlineFunctionSignatures) {
                val trueCallee = (callee as IrSimpleFunction).resolveFakeOverride()!!
                assertions.assertNotNull(trueCallee.body)
            }
            super.visitMemberAccess(expression)
        }
    }

}

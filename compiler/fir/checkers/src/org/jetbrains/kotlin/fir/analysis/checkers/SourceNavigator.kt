/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirLightSourceElement
import org.jetbrains.kotlin.fir.FirPsiSourceElement
import org.jetbrains.kotlin.fir.FirSourceElement
import org.jetbrains.kotlin.fir.analysis.diagnostics.getAncestors
import org.jetbrains.kotlin.fir.analysis.diagnostics.nameIdentifier
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.stubs.elements.KtNameReferenceExpressionElementType
import org.jetbrains.kotlin.psi.stubs.elements.KtTypeProjectionElementType

/**
 * Service to answer source-related questions in generic fashion.
 * Shouldn't expose (receive or return) any specific source tree types
 */
interface SourceNavigator {

    fun FirTypeRef.isInConstructorCallee(): Boolean

    fun FirTypeRef.isInTypeConstraint(): Boolean

    fun FirSourceElement.getRawIdentifier(): String?

    fun FirDeclaration.getRawName(): String?

    fun FirValueParameterSymbol.isCatchElementParameter(): Boolean

    companion object {

        private val lightTreeInstance = LightTreeSourceNavigator()

        fun forElement(e: FirElement): SourceNavigator = forSource(e.source)

        fun forSource(e: FirSourceElement?): SourceNavigator = when (e) {
            is FirLightSourceElement -> lightTreeInstance
            is FirPsiSourceElement -> PsiSourceNavigator
            null -> lightTreeInstance //shouldn't matter
        }

        inline fun <R> FirElement.withNavigator(block: SourceNavigator.() -> R): R = with(forSource(this.source), block)

        inline fun <R> FirSourceElement.withNavigator(block: SourceNavigator.() -> R): R = with(forSource(this), block)
    }
}

open class LightTreeSourceNavigator : SourceNavigator {

    private fun <T> FirElement.withSource(f: (FirSourceElement) -> T): T? =
        source?.let { f(it) }

    override fun FirTypeRef.isInConstructorCallee(): Boolean = withSource { source ->
        source.treeStructure.getParent(source.lighterASTNode)?.tokenType == KtNodeTypes.CONSTRUCTOR_CALLEE
    } ?: false

    override fun FirTypeRef.isInTypeConstraint(): Boolean {
        val source = source ?: return false
        return source.treeStructure.getAncestors(source.lighterASTNode)
            .find { it.tokenType == KtNodeTypes.TYPE_CONSTRAINT || it.tokenType == KtNodeTypes.TYPE_PARAMETER }
            ?.tokenType == KtNodeTypes.TYPE_CONSTRAINT
    }

    override fun FirSourceElement.getRawIdentifier(): String? {
        val tokenType = elementType
        return if (tokenType is KtNameReferenceExpressionElementType || tokenType == KtTokens.IDENTIFIER) {
            lighterASTNode.toString()
        } else if (tokenType is KtTypeProjectionElementType) {
            lighterASTNode.getChildren(treeStructure).last().toString()
        } else {
            null
        }
    }

    override fun FirDeclaration.getRawName(): String? {
        return source?.let { it.treeStructure.nameIdentifier(it.lighterASTNode)?.toString() }
    }

    override fun FirValueParameterSymbol.isCatchElementParameter(): Boolean {
        val localSource = source ?: return false
        var parent = localSource.treeStructure.getParent(localSource.lighterASTNode)
        parent?.let { parent = localSource.treeStructure.getParent(it) }
        return parent?.tokenType == KtNodeTypes.CATCH
    }
}

//by default psi tree can reuse light tree manipulations
object PsiSourceNavigator : LightTreeSourceNavigator() {

    //Swallows incorrect casts!!!
    private inline fun <reified P : PsiElement> FirElement.psi(): P? = source?.psi()

    private inline fun <reified P : PsiElement> FirSourceElement.psi(): P? {
        val psi = (this as? FirPsiSourceElement)?.psi
        return psi as? P
    }

    override fun FirTypeRef.isInConstructorCallee(): Boolean = psi<KtTypeReference>()?.parent is KtConstructorCalleeExpression

    override fun FirSourceElement.getRawIdentifier(): String? {
        val psi = psi<PsiElement>()
        return if (psi is KtNameReferenceExpression) {
            psi.getReferencedNameElement().node.text
        } else if (psi is KtTypeProjection) {
            psi.typeReference?.typeElement?.text
        } else if (psi is LeafPsiElement && psi.elementType == KtTokens.IDENTIFIER) {
            psi.text
        } else {
            null
        }
    }

    override fun FirDeclaration.getRawName(): String? {
        return (this.psi() as? PsiNameIdentifierOwner)?.nameIdentifier?.text
    }

    override fun FirValueParameterSymbol.isCatchElementParameter(): Boolean {
        return source?.psi<PsiElement>()?.parent?.parent is KtCatchClause
    }
}

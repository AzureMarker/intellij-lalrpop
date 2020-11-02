package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.util.computeType

abstract class LpSymbol1Mixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol1 {
    override fun internalResolveType(arguments: List<NonterminalGenericArgument>): String {
        val nonterminalRef = this.nonterminalRef
        if (nonterminalRef != null) {
            return (nonterminalRef.reference?.resolve()?.parent as? LpNonterminal)?.resolveType(arguments) ?: "()"
        }

        val quotedTerminal = this.quotedTerminal
        if (quotedTerminal != null) {
            // FIXME: resolve when terminals' type are resolved
            return "&str"
        }

        val parenthesisExprSymbol = this.parenthesisExprSymbol
        if (parenthesisExprSymbol != null) {
            return (parenthesisExprSymbol.exprSymbol.symbolList.computeType())
        }

        return when (this.firstChild) {
            LpElementTypes.LOOKAHEAD, LpElementTypes.LOOKBEHIND -> "usize"
            LpElementTypes.NOT -> "()"
            else -> "()"
        }
    }

    override val needsParameterNames: Boolean = true

    override fun completeParameterNames(arguments: List<NonterminalGenericArgument>): List<NonterminalGenericArgument> {
        return (parent as LpResolveType).completeParameterNames(arguments)
    }
}
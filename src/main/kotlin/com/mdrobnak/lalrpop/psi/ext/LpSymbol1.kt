package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpSymbol1
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import com.mdrobnak.lalrpop.psi.util.computeType
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

abstract class LpSymbol1Mixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol1 {
    override fun resolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String {
        val nonterminalRef = this.nonterminalRef
        if (nonterminalRef != null) {
            return nonterminalRef.resolveType(context, arguments)
        }

        val quotedTerminal = this.quotedTerminal
        if (quotedTerminal != null) {
            // FIXME: resolve when terminals' type are resolved
            return "&str"
        }

        val parenthesisExprSymbol = this.parenthesesExprSymbol
        if (parenthesisExprSymbol != null) {
            return (parenthesisExprSymbol.exprSymbol.symbolList.computeType(context, arguments))
        }

        return when (this.childrenWithLeaves.first().elementType) {
            LpElementTypes.LOOKAHEAD, LpElementTypes.LOOKBEHIND -> context.locationType
            LpElementTypes.NOT -> context.errorRecovery
            else -> "()"
        }
    }
}
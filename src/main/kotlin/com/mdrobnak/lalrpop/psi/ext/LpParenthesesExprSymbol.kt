package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpParenthesesExprSymbol
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpParenthesesExprSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpParenthesesExprSymbol {
    override fun resolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String {
        return this.exprSymbol.resolveType(context, arguments)
    }
}
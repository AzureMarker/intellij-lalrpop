package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpParenthesisExprSymbol
import com.mdrobnak.lalrpop.psi.LpResolveType
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpParenthesisExprSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpParenthesisExprSymbol {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String {
        return this.exprSymbol.resolveType(arguments)
    }
}
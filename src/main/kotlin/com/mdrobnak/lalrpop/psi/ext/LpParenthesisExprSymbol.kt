package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpParenthesisExprSymbol
import com.mdrobnak.lalrpop.psi.LpResolveType
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpParenthesisExprSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpParenthesisExprSymbol {
    override fun internalResolveType(arguments: List<NonterminalGenericArgument>): String {
        return this.exprSymbol.resolveType(arguments)
    }

    override fun completeParameterNames(arguments: List<NonterminalGenericArgument>): List<NonterminalGenericArgument> =
        (parent as LpResolveType).completeParameterNames(arguments)

    override val needsParameterNames: Boolean = true
}
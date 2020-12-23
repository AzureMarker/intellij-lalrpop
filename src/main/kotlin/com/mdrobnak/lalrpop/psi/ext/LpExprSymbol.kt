package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpExprSymbol
import com.mdrobnak.lalrpop.psi.LpResolveType
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import com.mdrobnak.lalrpop.psi.util.computeType

abstract class LpExprSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpExprSymbol {
    override fun resolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String =
        this.symbolList.computeType(context, arguments)
}
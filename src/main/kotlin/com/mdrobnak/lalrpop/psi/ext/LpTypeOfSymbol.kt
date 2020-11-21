package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpTypeOfSymbol
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpTypeOfSymbolMixin(node: ASTNode): ASTWrapperPsiElement(node), LpTypeOfSymbol {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String = this.symbol.resolveType(arguments)
}
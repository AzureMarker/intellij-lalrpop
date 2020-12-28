package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpTypeOfSymbol
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.LpMacroArguments

abstract class LpTypeOfSymbolMixin(node: ASTNode): ASTWrapperPsiElement(node), LpTypeOfSymbol {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String = this.symbol.resolveType(context, arguments)
}
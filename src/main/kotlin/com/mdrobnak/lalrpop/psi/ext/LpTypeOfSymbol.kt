package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpTypeOfSymbol
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext

abstract class LpTypeOfSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpTypeOfSymbol {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        symbol.resolveType(context, arguments)
}
package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpExprSymbol
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.util.computeType

abstract class LpExprSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpExprSymbol {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        this.symbolList.computeType(context, arguments)
}
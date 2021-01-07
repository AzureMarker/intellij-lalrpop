package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpArray
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.LpMacroArguments

abstract class LpArrayMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpArray {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        "[" + typeRef.resolveType(context, arguments) + "]"
}
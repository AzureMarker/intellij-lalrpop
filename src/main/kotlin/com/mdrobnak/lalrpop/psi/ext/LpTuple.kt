package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpTuple
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.LpMacroArguments

abstract class LpTupleMixin(node: ASTNode): ASTWrapperPsiElement(node), LpTuple {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        typeRefList.joinToString(prefix = "(", separator = ", ", postfix = ")") {
            it.resolveType(context, arguments)
        }
}
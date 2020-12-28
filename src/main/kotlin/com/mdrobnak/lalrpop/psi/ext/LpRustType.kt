package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpRustType
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.LpMacroArguments

abstract class LpRustTypeMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpRustType {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String {
        return (path.pathId?.resolveType(context, arguments) ?: path.text) +
                (this.typeGenericArguments?.let { genericArguments ->
                    (genericArguments.lifetimeRuleList.map { it.text } +
                            genericArguments.typeRefList.map { it.resolveType(context, arguments) })
                        .joinToString(prefix = "<", separator = ", ", postfix = ">") { it }
                } ?: "")
    }
}
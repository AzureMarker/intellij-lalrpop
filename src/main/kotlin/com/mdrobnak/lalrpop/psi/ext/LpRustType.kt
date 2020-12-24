package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpRustType
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpRustTypeMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpRustType {
    override fun resolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String {
        val path = this.path
        return (path.pathId?.resolveType(context, arguments) ?: path.text) +
                when (val genericArguments = this.typeGenericArguments) {
                    null -> ""
                    else -> (genericArguments.lifetimeRuleList.map { it.text } +
                            genericArguments.typeRefList.map { it.resolveType(context, arguments) })
                        .joinToString(prefix = "<", separator = ", ", postfix = ">") { it }
                }
    }
}
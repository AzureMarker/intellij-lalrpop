package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpRustType
import com.mdrobnak.lalrpop.psi.LpTypeRef
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext

abstract class LpRustTypeMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpRustType {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String {
        return (path.pathId?.resolveType(context, arguments) ?: path.text) +
                (this.typeGenericArguments?.let { genericArguments ->
                    genericArguments.typeRefOrLifetimeList
                        .joinToString(prefix = "<", separator = ", ", postfix = ">") {
                            when (val child = it.firstChild) {
                                is LpTypeRef -> child.resolveType(context, arguments)
                                else -> child.text // lifetime
                            }
                        }
                } ?: "")
    }
}
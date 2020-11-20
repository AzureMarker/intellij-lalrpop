package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpDynTrait
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpDynTraitMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpDynTrait {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String {
        val path = this.path
        val typeGenericArguments = this.typeGenericArguments
        return if (typeGenericArguments == null)
            "dyn " + path.text
        else {
            "dyn " + path.text + (
                    typeGenericArguments.lifetimeRuleList.map { it.text } +
                            typeGenericArguments.typeRefList.map { it.resolveType(arguments) }
                    ).joinToString(prefix = "<", separator = ", ", postfix = ">") { it }
        }
    }
}
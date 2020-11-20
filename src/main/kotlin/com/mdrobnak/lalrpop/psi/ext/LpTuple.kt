package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpTuple
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpTupleMixin(node: ASTNode): ASTWrapperPsiElement(node), LpTuple {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String =
        this.typeRefList.joinToString(prefix = "(", separator = ", ", postfix = ")") {
            it.resolveType(arguments)
        }
}
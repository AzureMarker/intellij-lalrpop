package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpArray
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpArrayMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpArray {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String =
        "[" + this.typeRef.resolveType(arguments) + "]"
}
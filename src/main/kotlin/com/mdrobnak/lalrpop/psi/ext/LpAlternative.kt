package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpAlternative
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import com.mdrobnak.lalrpop.psi.util.computeType

abstract class LpAlternativeMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpAlternative {
    override val needsParameterNames: Boolean = true

    override fun completeParameterNames(arguments: List<NonterminalGenericArgument>): List<NonterminalGenericArgument> {
        return (this.parent // alternatives
            .parent // nonterminal
                as LpNonterminal).completeParameterNames(arguments)
    }

    override fun internalResolveType(arguments: List<NonterminalGenericArgument>): String {
        return this.symbolList.computeType()
    }
}
package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.LpAlternative
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpNonterminalMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminal {
    override fun internalResolveType(arguments: List<NonterminalGenericArgument>): String {
        val typeRef = this.typeRef
        if (typeRef != null) return typeRef.text
        val alternative: LpAlternative =
            PsiTreeUtil.findChildrenOfType(this.alternatives, LpAlternative::class.java)
                .firstOrNull { it.action != null } ?: return "()"
        return alternative.resolveType(arguments)
    }

    override fun completeParameterNames(arguments: List<NonterminalGenericArgument>): List<NonterminalGenericArgument> {
        val parameters = this.nonterminalName.nonterminalParams?.nonterminalParamList
        return parameters?.zip(arguments)?.map {
            NonterminalGenericArgument(it.second.rustType, it.first.text)
        } ?: listOf()
    }

    override val needsParameterNames: Boolean = true
}
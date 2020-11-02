package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.mdrobnak.lalrpop.psi.LpNonterminalName
import com.mdrobnak.lalrpop.psi.LpNonterminalRef
import com.mdrobnak.lalrpop.psi.LpResolveType
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import com.mdrobnak.lalrpop.psi.util.arguments
import com.mdrobnak.lalrpop.psi.util.nonterminalParent
import com.mdrobnak.lalrpop.resolve.LpNonterminalReference

abstract class LpNonterminalRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminalRef {
    override fun getReference(): PsiReference {
        return LpNonterminalReference(this)
    }

    override val needsParameterNames: Boolean = true

    override fun completeParameterNames(arguments: List<NonterminalGenericArgument>): List<NonterminalGenericArgument> {
        return (parent as LpResolveType).completeParameterNames(arguments)
    }

    override fun internalResolveType(arguments: List<NonterminalGenericArgument>): String {
        return when (val ref = this.reference.resolve()) {
            is LpNonterminalName -> {
                val nonterminalParams = ref.nonterminalParams
                    ?: return ref.nonterminalParent.resolveType(listOf())

                val nonterminalParent = ref.nonterminalParent

                val nonterminalArguments = this.arguments
                if (nonterminalArguments != null) {
                    nonterminalParent.resolveType(nonterminalArguments.symbolList.map {
                        NonterminalGenericArgument(it.resolveType(arguments))
                    })
                } else {
                    nonterminalParent.resolveType(nonterminalParams.nonterminalParamList.map {
                        NonterminalGenericArgument(
                            "()"
                        )
                    })
                }
            }
            else -> "()"
        }
    }
}

package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.mdrobnak.lalrpop.psi.LpNonterminalName
import com.mdrobnak.lalrpop.psi.LpNonterminalParam
import com.mdrobnak.lalrpop.psi.LpNonterminalRef
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import com.mdrobnak.lalrpop.psi.util.arguments
import com.mdrobnak.lalrpop.psi.util.nonterminalParent
import com.mdrobnak.lalrpop.resolve.LpNonterminalReference

abstract class LpNonterminalRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminalRef {
    override fun getReference(): PsiReference {
        return LpNonterminalReference(this)
    }

    override fun resolveType(arguments: List<NonterminalGenericArgument>): String {
        return when (val ref = this.reference.resolve()) {
            is LpNonterminalName -> {
                val nonterminalParams = ref.nonterminalParams
                    ?: return ref.nonterminalParent.resolveType(arguments)

                val nonterminal = ref.nonterminalParent

                val nonterminalArguments = this.arguments
                if (nonterminalArguments != null) {
                    nonterminal.resolveType(
                        nonterminalArguments.symbolList.zip(nonterminalParams.nonterminalParamList).map {
                            NonterminalGenericArgument(it.first.resolveType(arguments), it.second.text)
                        })
                } else {
                    nonterminal.resolveType(nonterminalParams.nonterminalParamList.map {
                        NonterminalGenericArgument("()", it.text)
                    })
                }
            }
            is LpNonterminalParam -> {
                val result = arguments.find { it.name == ref.text }?.rustType ?: ref.text
                result
            }
            else -> "()"
        }
    }
}

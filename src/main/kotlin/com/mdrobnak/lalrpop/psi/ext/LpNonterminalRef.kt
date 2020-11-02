package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.mdrobnak.lalrpop.psi.*
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

                val nonterminal = ref.nonterminalParent

                val nonterminalArguments = this.arguments
                println("Nonterminal arguments: $nonterminalArguments")
                if (nonterminalArguments != null) {
                    nonterminal.resolveType(nonterminalArguments.symbolList.map {
                        NonterminalGenericArgument(it.resolveType(arguments))
                    })
                } else {
                    nonterminal.resolveType(nonterminalParams.nonterminalParamList.map {
                        NonterminalGenericArgument("()")
                    })
                }
            }
            is LpNonterminalParam -> {
                val result = arguments.find { it.name == ref.text }?.rustType ?: "()"
                println("Found nonterminal param ${ref.text}, replaced with $result")
                result
            }
            else -> "()"
        }
    }
}

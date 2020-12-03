package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.resolve.LpNonterminalReference

val LpNonterminalRef.arguments: LpNonterminalArguments?
    get() = this.nextSibling as? LpNonterminalArguments

fun LpNonterminalRef.createNonterminal() {
    val factory = LpElementFactory(project)

    val nonterminal = this.parentOfType<LpNonterminal>() ?: return
    val grammar = nonterminal.parent

    grammar.addAfter(
        factory.createNonterminal(this.text, this.arguments?.symbolList?.mapIndexed { index, _ -> "Rule${index+1}" }),
        nonterminal,
    )
}

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
                arguments.find { it.name == ref.text }?.rustType ?: ref.text
            }
            else -> "()"
        }
    }
}

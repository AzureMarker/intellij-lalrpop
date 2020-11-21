package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpAlternative
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpSymbol
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import com.mdrobnak.lalrpop.psi.util.computeType
import com.mdrobnak.lalrpop.psi.util.selected

val LpAlternative.selected: List<LpSymbol>
    get() {
        return this.children.filterIsInstance<LpSymbol>().selected
    }

abstract class LpAlternativeMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpAlternative {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String {
        return this.symbolList.computeType(arguments)
    }
}
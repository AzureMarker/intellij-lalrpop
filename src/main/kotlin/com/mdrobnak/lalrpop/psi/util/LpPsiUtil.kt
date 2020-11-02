package com.mdrobnak.lalrpop.psi.util

import com.mdrobnak.lalrpop.psi.LpGrammarParam
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.*
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

fun LpGrammarParam.name: String
        get() = this.firstChild.text

val LpNonterminalName.nonterminal: LpNonterminal
    get() = parent as LpNonterminal

val LpSymbol.isExplicitlySelected: Boolean
    get() = this.childrenWithLeaves.first().elementType == LpElementTypes.LESSTHAN
val LpSymbol.isSelected: Boolean
    get() = (this.parent as LpAlternative).selected.any { it == this }


val LpNonterminal.typeOrCompute: String?
    get() {
        val typeRef = this.typeRef
        if (typeRef != null) return typeRef.text
        return this.computeType()
    }

fun LpNonterminal.computeType(): String? {
    val alternative: LpAlternative =
        PsiTreeUtil.findChildOfType(this.alternatives, LpAlternative::class.java) ?: return null
    return alternative.computeType()
}

val LpAlternative.selected: List<LpSymbol>
    get() {
        return if (this.children.any { it is LpSymbol && it.isExplicitlySelected }) {
            this.children.filterIsInstance<LpSymbol>().filter { it.isExplicitlySelected }
        } else {
            this.children.filterIsInstance<LpSymbol>()
        }
    }

val LpNonterminalRef.arguments: LpNonterminalArguments?
    get() = this.nextSibling as? LpNonterminalArguments

val LpNonterminalName.nonterminalParent: LpNonterminal
    get() = this.parent as LpNonterminal

fun LpAlternative.computeType(): String {
    val selectedList = this.selected
    return selectedList.computeType()
}

fun List<LpSymbol>.computeType(): String {
    return joinToString(
        prefix = if (size != 1) "(" else "",
        postfix = if (size != 1) ")" else ""
    ) {
        it.resolveType()
    }
}

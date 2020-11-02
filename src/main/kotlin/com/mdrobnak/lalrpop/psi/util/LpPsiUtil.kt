package com.mdrobnak.lalrpop.psi.util

import com.mdrobnak.lalrpop.psi.*
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

val LpGrammarParam.name: String
    get() = this.firstChild.text

val LpNonterminalName.nonterminal: LpNonterminal
    get() = parent as LpNonterminal

val LpSymbol.isExplicitlySelected: Boolean
    get() = this.childrenWithLeaves.first().elementType == LpElementTypes.LESSTHAN
val LpSymbol.isSelected: Boolean
    get() = (this.parent as LpAlternative).selected.any { it == this }


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

val LpTypeRef.lifetime: String?
    get() = this.childrenWithLeaves.find { it.elementType == LpElementTypes.LIFETIME }?.text

val LpTypeRef.lifetimeOrInfer: String
    get() = this.lifetime ?: "'_"

val LpTypeRef.isRefMut: Boolean
    get() = this.childrenWithLeaves.find { it.elementType == LpElementTypes.MUT } != null

fun LpAlternative.computeType(): String {
    val selectedList = this.selected
    return selectedList.computeType()
}

fun List<LpSymbol>.computeType(arguments: List<NonterminalGenericArgument> = listOf()): String {
    return joinToString(
        prefix = if (size != 1) "(" else "",
        postfix = if (size != 1) ")" else ""
    ) {
        it.resolveType(arguments)
    }
}

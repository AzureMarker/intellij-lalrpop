package com.mdrobnak.lalrpop.psi.util

import com.mdrobnak.lalrpop.psi.LpSymbol
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import com.mdrobnak.lalrpop.psi.ext.isExplicitlySelected

val List<LpSymbol>.selected: List<LpSymbol>
    get() = if (this.any { it.isExplicitlySelected }) {
        this.filter { it.isExplicitlySelected }
    } else {
        this
    }

fun List<LpSymbol>.computeType(arguments: List<NonterminalGenericArgument>): String {
    val sel = selected
    return sel.joinToString(
        prefix = if (sel.size != 1) "(" else "",
        postfix = if (sel.size != 1) ")" else ""
    ) {
        it.resolveType(arguments)
    }
}

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


val LpAlternative.selected: List<LpSymbol>
    get() {
        return this.children.filterIsInstance<LpSymbol>().selected
    }

val LpNonterminalRef.arguments: LpNonterminalArguments?
    get() = this.nextSibling as? LpNonterminalArguments

val LpNonterminalName.nonterminalParent: LpNonterminal
    get() = this.parent as LpNonterminal

val LpRustReference.lifetime: String?
    get() = this.childrenWithLeaves.find { it.elementType == LpElementTypes.LIFETIME }?.text

val LpRustReference.lifetimeOrInfer: String
    get() = this.lifetime ?: "'_"

val LpRustReference.isRefMut: Boolean
    get() = this.childrenWithLeaves.find { it.elementType == LpElementTypes.MUT } != null

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

val LpAction.alternativeParent: LpAlternative
    get() = this.parent as LpAlternative

fun <T> LpRepeatOp.switch(question: T, plus: T, multiply: T): T =
    when (this.childrenWithLeaves.first().elementType) {
        LpElementTypes.QUESTION -> question
        LpElementTypes.PLUS -> plus
        LpElementTypes.MULTIPLY -> multiply
        else -> TODO("Unreachable") //TODO: should be something like rust's `unreachable!()` but I have no idea how to tell the kotlin compiler about that
    }

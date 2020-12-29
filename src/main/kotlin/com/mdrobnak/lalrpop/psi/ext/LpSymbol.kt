package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.*
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

val LpSymbol.isExplicitlySelected: Boolean
    get() = this.lessthan != null

val LpSymbol.isNamed: Boolean
    get() = this.symbolName != null

val LpSymbol.isMutable: Boolean
    get() = this.symbolName?.mut != null

val LpSymbol.symbolNameString: String?
    get() = this.symbolName?.id?.text

fun LpSymbol.removeName() {
    this.symbolName?.delete()
}

fun LpSymbol.getSelectedType(context: LpTypeResolutionContext): LpSelectedType {
    val isMutable = this.isMutable
    val name = this.symbolNameString
    val type = this.resolveType(context, LpMacroArguments.identity(this.parentOfType<LpNonterminal>()?.nonterminalName?.nonterminalParams))

    return if (name != null) {
        LpSelectedType.WithName(isMutable, name, type)
    } else {
        LpSelectedType.WithoutName(type)
    }
}

abstract class LpSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        symbol0.resolveType(context, arguments)
}

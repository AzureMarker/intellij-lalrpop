package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.*

val LpSymbol.isExplicitlySelected: Boolean
    get() = lessthan != null

val LpSymbol.isNamed: Boolean
    get() = symbolName != null

val LpSymbol.isMutable: Boolean
    get() = symbolName?.mut != null

val LpSymbol.symbolNameString: String?
    get() = symbolName?.id?.text

fun LpSymbol.removeName() {
    symbolName?.delete()
}

fun LpSymbol.getSelectedType(context: LpTypeResolutionContext, resolveTypes: Boolean = true): LpSelectedType {
    val isMutable = this.isMutable
    val name = this.symbolNameString
    val type = if (resolveTypes)
        resolveType(
            context,
            LpMacroArguments.identity(parentOfType<LpNonterminal>()?.nonterminalName?.nonterminalParams)
        )
    else ""

    return if (name != null)
        LpSelectedType.WithName(name, type, isMutable)
    else
        LpSelectedType.WithoutName(type)
}

abstract class LpSymbolMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        symbol0.resolveType(context, arguments)
}

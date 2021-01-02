package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.parentOfType
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.util.computeType
import com.mdrobnak.lalrpop.psi.util.lalrpopTypeResolutionContext
import com.mdrobnak.lalrpop.psi.util.selected

val LpAlternative.selected: List<LpSymbol>
    get() = this.symbolList.selected

/**
 * The selected types, resolved with the 'context' resolution context, and
 *
 * @param context The type resolution context
 * @param resolveTypes Whether to resolve types or not. Some calls to this(like in 'LpAction.createLiteralTextEscaper'),
 * don't need to know about the types, but are only interested in names.
 *
 * @see LpTypeResolutionContext
 * @see LpAction.createLiteralTextEscaper
 */
fun LpAlternative.selectedTypesInContext(
    context: LpTypeResolutionContext = containingFile.lalrpopTypeResolutionContext(),
    resolveTypes: Boolean = true
): List<LpSelectedType> =
    this.selected.map { it.getSelectedType(context, resolveTypes) }

val LpAlternative.nonterminalParent: LpNonterminal
    get() = this.parentOfType()!!

abstract class LpAlternativeMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpAlternative {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        this.action?.resolveType(context, arguments) ?: this.symbolList.computeType(context, arguments)
}
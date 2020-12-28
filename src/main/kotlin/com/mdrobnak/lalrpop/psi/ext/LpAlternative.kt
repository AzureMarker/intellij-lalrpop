package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.util.computeType
import com.mdrobnak.lalrpop.psi.util.lalrpopTypeResolutionContext
import com.mdrobnak.lalrpop.psi.util.selected

val LpAlternative.selected: List<LpSymbol>
    get() = this.symbolList.selected

fun LpAlternative.selectedTypesInContext(context: LpTypeResolutionContext = containingFile.lalrpopTypeResolutionContext()): List<LpSelectedType> =
    this.selected.map { it.getSelectedType(context) }

abstract class LpAlternativeMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpAlternative {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        this.action?.resolveType(context, arguments) ?: this.symbolList.computeType(context, arguments)
}
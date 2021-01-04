package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.*
import com.mdrobnak.lalrpop.psi.util.computeType
import org.rust.lang.core.psi.ext.childrenWithLeaves
import org.rust.lang.core.psi.ext.elementType

abstract class LpSymbol1Mixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol1 {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String {
        nonterminalRef?.let { return it.resolveType(context, arguments) }

        quotedTerminal?.let {
            // FIXME: resolve when terminals' type are resolved
            return "&str"
        }

        parenthesesExprSymbol?.let { return (it.exprSymbol.symbolList.computeType(context, arguments)) }

        return when (childrenWithLeaves.first().elementType) {
            LpElementTypes.LOOKAHEAD, LpElementTypes.LOOKBEHIND -> context.locationType
            LpElementTypes.NOT -> context.errorRecovery
            else -> "()"
        }
    }
}
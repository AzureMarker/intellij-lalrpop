package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpElementTypes
import com.mdrobnak.lalrpop.psi.LpResolveType
import com.mdrobnak.lalrpop.psi.LpSymbol0
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument
import org.rust.lang.core.psi.ext.elementType

abstract class LpSymbol0Mixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol0 {
    override fun internalResolveType(arguments: List<NonterminalGenericArgument>): String {
        var tp = symbol1.resolveType(arguments)
        for (repeatOp in repeatOpList) {
            tp = when (repeatOp.elementType) {
                LpElementTypes.QUESTION -> "::std::option::Option<$tp>"
                LpElementTypes.PLUS, LpElementTypes.MULTIPLY -> "::std::vec::Vec<$tp>"
                else -> tp // should never happen but kotlin doesn't know this
            }
        }
        return tp
    }

    override val needsParameterNames: Boolean = true

    override fun completeParameterNames(arguments: List<NonterminalGenericArgument>): List<NonterminalGenericArgument> {
        return (parent as LpResolveType).completeParameterNames(arguments)
    }
}
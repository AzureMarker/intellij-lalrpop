package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpSymbol0
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpSymbol0Mixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol0 {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String {
        var tp = symbol1.resolveType(arguments)
        for (repeatOp in repeatOpList) {
            tp = repeatOp.switch(
                question = "::std::option::Option<$tp>",
                multiply = "::std::vec::Vec<$tp>",
                plus = "::std::vec::Vec<$tp>"
            )
        }
        return tp
    }
}
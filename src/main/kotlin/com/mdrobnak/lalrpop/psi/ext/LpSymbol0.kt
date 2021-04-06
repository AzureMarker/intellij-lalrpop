package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpSymbol0
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.LpMacroArguments

abstract class LpSymbol0Mixin(node: ASTNode) : ASTWrapperPsiElement(node), LpSymbol0 {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        repeatOpList.fold(symbol1.resolveType(context, arguments)) { tp, repeatOp ->
            repeatOp.switch(
                question = "::core::option::Option<$tp>",
                multiply = "::alloc::vec::Vec<$tp>",
                plus = "::alloc::vec::Vec<$tp>"
            )
        }
}
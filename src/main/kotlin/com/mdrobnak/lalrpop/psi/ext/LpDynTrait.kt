package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.mdrobnak.lalrpop.psi.LpDynTrait
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpDynTraitMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpDynTrait {
    override fun resolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String {
        return "dyn " + this.rustType.resolveType(context, arguments)
    }
}
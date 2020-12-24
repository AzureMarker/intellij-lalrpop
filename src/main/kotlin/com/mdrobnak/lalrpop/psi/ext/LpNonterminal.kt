package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpNonterminalMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminal {
    override fun resolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String =
        if (this.nonterminalName.nonterminalParams != null) {
            internallyResolveType(context, arguments)
        } else {
            // Isn't a lalrpop macro and therefore can be cached
            CachedValuesManager.getCachedValue(this) {
                return@getCachedValue CachedValueProvider.Result<String>(
                    internallyResolveType(context, listOf()),
                    PsiModificationTracker.MODIFICATION_COUNT
                )
            }
        }

    private fun internallyResolveType(context: LpTypeResolutionContext, arguments: List<NonterminalGenericArgument>): String =
        this.typeRef?.resolveType(context, arguments) ?: this.alternatives.alternativeList.firstOrNull { it.action == null }
            ?.resolveType(context, arguments) ?: "()"
}
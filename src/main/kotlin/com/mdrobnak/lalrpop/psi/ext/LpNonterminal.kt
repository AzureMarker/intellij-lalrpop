package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.mdrobnak.lalrpop.psi.LpAlternative
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpResolveType
import com.mdrobnak.lalrpop.psi.NonterminalGenericArgument

abstract class LpNonterminalMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminal {
    override fun resolveType(arguments: List<NonterminalGenericArgument>): String =
        if (this.nonterminalName.nonterminalParams != null) {
            internallyResolveType(arguments)
        } else {
            // Doesn't depend on any of the rule arguments given and therefore can be cached
            CachedValuesManager.getCachedValue(this) {
                return@getCachedValue CachedValueProvider.Result<String>(internallyResolveType(listOf()), PsiModificationTracker.MODIFICATION_COUNT)
            }
        }

    private fun internallyResolveType(arguments: List<NonterminalGenericArgument>): String {
        val typeRef = this.typeRef
        if (typeRef != null) return (typeRef as LpResolveType).resolveType(arguments)
        val alternative: LpAlternative =
            PsiTreeUtil.findChildrenOfType(this.alternatives, LpAlternative::class.java)
                .firstOrNull { it.action == null } ?: return "()"
        return alternative.resolveType(arguments)
    }
}
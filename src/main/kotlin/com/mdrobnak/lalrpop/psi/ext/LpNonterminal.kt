package com.mdrobnak.lalrpop.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.mdrobnak.lalrpop.psi.LpElementFactory
import com.mdrobnak.lalrpop.psi.LpMacroArguments
import com.mdrobnak.lalrpop.psi.LpNonterminal
import com.mdrobnak.lalrpop.psi.LpTypeResolutionContext

fun LpNonterminal.setType(type: String) {
    val typePsi = LpElementFactory(project).createNonterminalType(type)
    val typeRef = typeRef

    if (typeRef != null)
        typeRef.replace(typePsi.second)
    else
        addRangeAfter(typePsi.first, typePsi.second, nonterminalName)
}

abstract class LpNonterminalMixin(node: ASTNode) : ASTWrapperPsiElement(node), LpNonterminal {
    override fun resolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        if (this.nonterminalName.nonterminalParams != null) {
            internallyResolveType(context, arguments)
        } else {
            // Isn't a lalrpop macro and therefore can be cached
            CachedValuesManager.getCachedValue(this) {
                return@getCachedValue CachedValueProvider.Result<String>(
                    internallyResolveType(context, LpMacroArguments()),
                    PsiModificationTracker.MODIFICATION_COUNT
                )
            }
        }

    private fun internallyResolveType(context: LpTypeResolutionContext, arguments: LpMacroArguments): String =
        // get directly from the type_ref if available
        this.typeRef?.resolveType(context, arguments) ?:
        // or try to infer from the first alternative that doesn't have action code
        this.alternatives.alternativeList.firstOrNull { it.action == null }?.resolveType(context, arguments) ?:
        // or as a last resort try to infer from the action code with intellij-rust
        this.alternatives.alternativeList.firstOrNull()?.resolveType(context, arguments) ?:
        // or if we get here, it means the nonterminal looks like `A = {};`, e.g. there are no alternatives and no type_ref
        "()"
}